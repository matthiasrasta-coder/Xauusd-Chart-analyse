package com.example.views;

import java.util.ArrayList;
import java.util.List;

import com.example.chart.Candle;
import com.example.chart.CandlestickChart;
import com.example.chart.LineChart;
import com.example.chart.LinePoint;
import com.example.data.CandleService;
import com.example.data.Mt5Exception;
import com.example.data.Timeframe;
import com.example.formula.Formula;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Chart")
public class ChartView extends VerticalLayout {

    private static final String SYNC_GROUP = "xauusd";

    private final CandleService candleService;
    private final CandlestickChart candleChart = new CandlestickChart();
    private final FormulaInput formulaInput;
    private final LineChart resultChart = new LineChart();
    private List<Candle> candles = List.of();

    public ChartView(CandleService candleService) {
        this.candleService = candleService;
        setWidthFull();

        // 0) Zeitrahmen wählen / neu laden
        TimeframeBar timeframeBar = new TimeframeBar(Timeframe.H1, this::load);

        // 1) Kerzenchart
        candleChart.setWidthFull();
        candleChart.setHeight("450px");
        candleChart.setSyncGroup(SYNC_GROUP);

        // 2) Formel-Eingabe
        formulaInput = new FormulaInput("High - Low", this::showResult);

        // 3) Ergebnis-Chart, zoomt und verschiebt zusammen mit dem Kerzenchart
        resultChart.setWidthFull();
        resultChart.setHeight("250px");
        resultChart.setSyncGroup(SYNC_GROUP);

        add(timeframeBar, candleChart, formulaInput, resultChart);

        load(timeframeBar.getTimeframe());
    }

    /** Lädt die Kerzen aus MT5 und berechnet die Formel neu. */
    private void load(Timeframe timeframe) {
        try {
            candles = candleService.getCandles(timeframe);
        } catch (Mt5Exception e) {
            candles = List.of();
            Notifications.showError(e.getMessage());
        }
        candleChart.setCandles(candles);
        formulaInput.calculate();
    }

    private void showResult(Formula formula) {
        double[] values = formula.evaluate(candles);

        List<LinePoint> points = new ArrayList<>(values.length);
        for (int i = 0; i < values.length; i++) {
            double v = values[i];
            // NaN/Infinity (z. B. Division durch 0) → Lücke im Chart
            points.add(new LinePoint(candles.get(i).time(), Double.isFinite(v) ? v : null));
        }

        resultChart.setSeriesName(formula.text());
        resultChart.setPoints(points);
    }
}
