package com.example.formula;

import java.util.List;

import com.example.chart.Candle;

/**
 * Ein Teil einer Formel (Zahl, Variable, Rechenoperation ...), der für eine Kerze ausgewertet wird.
 * Übergeben werden alle Kerzen plus der Index der aktuellen Kerze – so können später auch
 * Formeln mit Bezug auf frühere Kerzen (z. B. "Close[1]" oder Durchschnitte) ergänzt werden.
 */
@FunctionalInterface
interface Expression {

    double eval(List<Candle> candles, int index);
}
