package com.example.stats;

/** Ein Balken im Histogramm: Anzahl der Werte im Bereich [from, to). */
public record HistogramBin(double from, double to, int count) {
}
