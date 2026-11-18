package com.example.formula;

import java.util.List;

import com.example.chart.Candle;

/** Eine fertig eingelesene Formel. Erzeugt wird sie mit {@link FormulaParser#parse(String)}. */
public final class Formula {

    private final String text;
    private final Expression root;

    Formula(String text, Expression root) {
        this.text = text;
        this.root = root;
    }

    public String text() {
        return text;
    }

    /**
     * Wertet die Formel für jede Kerze aus. Ergebnis hat genauso viele Einträge wie {@code candles}.
     * Ungültige Rechnungen (z. B. Division durch 0) liefern NaN oder Infinity.
     */
    public double[] evaluate(List<Candle> candles) {
        double[] result = new double[candles.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = root.eval(candles, i);
        }
        return result;
    }
}
