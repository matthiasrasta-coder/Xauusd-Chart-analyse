package com.example.chart;

/**
 * Ein Punkt im Linienchart. {@code value} ist {@code null}, wenn es keinen gültigen Wert gibt
 * (z. B. Division durch 0) – dann zeigt der Chart dort eine Lücke.
 */
public record LinePoint(long time, Double value) {
}
