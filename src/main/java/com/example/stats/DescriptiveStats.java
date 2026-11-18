package com.example.stats;

import java.util.Arrays;

/**
 * Kennzahlen einer Werteliste. Bei leerer Liste ist {@code count} 0 und die übrigen Werte sind NaN.
 * {@code stdDev} ist die Stichproben-Standardabweichung (Teiler n - 1).
 */
public record DescriptiveStats(int count, double sum, double mean, double median, double stdDev, double min,
        double max) {

    /** Erwartet nur endliche Werte (kein NaN/Infinity). */
    public static DescriptiveStats of(double[] values) {
        int n = values.length;
        if (n == 0) {
            return new DescriptiveStats(0, 0, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        }
        double[] sorted = values.clone();
        Arrays.sort(sorted);

        double sum = 0;
        for (double v : sorted) {
            sum += v;
        }
        double mean = sum / n;

        double squares = 0;
        for (double v : sorted) {
            squares += (v - mean) * (v - mean);
        }
        double stdDev = n > 1 ? Math.sqrt(squares / (n - 1)) : 0;

        double median = n % 2 == 1 ? sorted[n / 2] : (sorted[n / 2 - 1] + sorted[n / 2]) / 2;

        return new DescriptiveStats(n, sum, mean, median, stdDev, sorted[0], sorted[n - 1]);
    }
}
