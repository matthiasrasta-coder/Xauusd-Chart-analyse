package com.example.views;

import java.util.List;
import java.util.Locale;
import java.util.function.ToDoubleFunction;

import com.example.chart.Candle;
import com.example.chart.HistogramChart;
import com.example.data.CandleService;
import com.example.data.Mt5Exception;
import com.example.data.Timeframe;
import com.example.formula.Formula;
import com.example.stats.DescriptiveStats;
import com.example.stats.DirectionStatistics;
import com.example.stats.Histogram;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Statistik-Seite: berechnet pro Kerze einen Wert (Formel) und zeigt
 * die Aufteilung nach Richtung (Tabelle) und die Verteilung der Werte (Histogramm).
 */
@Route(value = "statistik", layout = MainLayout.class)
@PageTitle("Statistik")
public class StatisticsView extends VerticalLayout {

    private static final int DEFAULT_BINS = 30;

    /** Eine Tabellenzeile: Richtung, Kennzahlen, Anteil an allen gültigen Werten in Prozent. */
    private record DirectionRow(String direction, DescriptiveStats stats, double sharePercent) {
    }

    private final CandleService candleService;
    private final TimeframeBar timeframeBar;
    private List<Candle> candles = List.of();
    private final FormulaInput formulaInput;
    private final IntegerField binsField = new IntegerField("Anzahl Balken");
    private final Paragraph summary = new Paragraph();
    private final Grid<DirectionRow> grid = new Grid<>();
    private final HistogramChart histogram = new HistogramChart();
    private Formula currentFormula;

    public StatisticsView(CandleService candleService) {
        this.candleService = candleService;
        setWidthFull();

        timeframeBar = new TimeframeBar(Timeframe.H1, this::load);
        formulaInput = new FormulaInput("Close - Open", this::update);

        binsField.setValue(DEFAULT_BINS);
        binsField.setMin(2);
        binsField.setMax(200);
        binsField.setStepButtonsVisible(true);
        binsField.setWidth("10em");
        binsField.addValueChangeListener(e -> {
            if (currentFormula != null) {
                update(currentFormula);
            }
        });
        formulaInput.addBeforeButton(binsField);

        Paragraph explanation = new Paragraph("Die Formel liefert pro Kerze einen Wert. Richtung = Vorzeichen: "
                + "Wert > 0 = aufwärts, < 0 = abwärts. Mit „Close - Open“ sind das grüne bzw. rote Kerzen "
                + "und ihre Körpergröße.");

        configureGrid();

        histogram.setWidthFull();
        histogram.setHeight("350px");

        add(timeframeBar, formulaInput, explanation, new H3("Richtungen"), summary, grid,
                new H3("Verteilung der Werte"), histogram);

        load(timeframeBar.getTimeframe());
    }

    /** Lädt die Kerzen aus MT5 und berechnet die Statistik neu. */
    private void load(Timeframe timeframe) {
        try {
            candles = candleService.getCandles(timeframe);
        } catch (Mt5Exception e) {
            candles = List.of();
            Notifications.showError(e.getMessage());
        }
        formulaInput.calculate();
    }

    private void configureGrid() {
        grid.addColumn(DirectionRow::direction).setHeader("Richtung").setAutoWidth(true);
        grid.addColumn(r -> r.stats().count()).setHeader("Anzahl");
        grid.addColumn(r -> fmt(r.sharePercent()) + " %").setHeader("Anteil");
        addNumberColumn("Mittelwert", DescriptiveStats::mean);
        addNumberColumn("Median", DescriptiveStats::median);
        addNumberColumn("Std.-Abw.", DescriptiveStats::stdDev);
        addNumberColumn("Min", DescriptiveStats::min);
        addNumberColumn("Max", DescriptiveStats::max);
        addNumberColumn("Summe", DescriptiveStats::sum);
        grid.setAllRowsVisible(true); // Höhe passt sich den Zeilen an, kein Scrollbalken
    }

    private void addNumberColumn(String header, ToDoubleFunction<DescriptiveStats> getter) {
        grid.addColumn(r -> fmt(getter.applyAsDouble(r.stats()))).setHeader(header);
    }

    private void update(Formula formula) {
        currentFormula = formula;
        double[] values = formula.evaluate(candles);

        DirectionStatistics stats = DirectionStatistics.of(values);
        int total = stats.all().count();
        grid.setItems(
                row("▲ Aufwärts (> 0)", stats.up(), total),
                row("▼ Abwärts (< 0)", stats.down(), total),
                row("■ Neutral (= 0)", stats.neutral(), total),
                row("Σ Gesamt", stats.all(), total));

        String text = candleService.getSymbol() + " " + timeframeBar.getTimeframe().name()
                + " · " + candles.size() + " Kerzen · Formel: " + formula.text() + " · " + total + " gültige Werte";
        if (stats.invalidCount() > 0) {
            text += " · " + stats.invalidCount() + " ungültige ausgelassen (z. B. Division durch 0)";
        }
        summary.setText(text);

        Integer bins = binsField.getValue();
        histogram.setBins(Histogram.of(values, bins != null && bins >= 2 ? bins : DEFAULT_BINS));
    }

    private static DirectionRow row(String name, DescriptiveStats stats, int total) {
        return new DirectionRow(name, stats, total == 0 ? 0 : 100.0 * stats.count() / total);
    }

    private static String fmt(double value) {
        return Double.isFinite(value) ? String.format(Locale.GERMANY, "%,.2f", value) : "–";
    }
}
