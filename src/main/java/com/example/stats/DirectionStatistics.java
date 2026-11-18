package com.example.stats;

import java.util.Arrays;

/**
 * Teilt Werte nach Richtung auf: aufwärts (&gt; 0), abwärts (&lt; 0), neutral (= 0).
 * Ungültige Werte (NaN/Infinity, z. B. durch Division durch 0) werden nur gezählt.
 */
public record DirectionStatistics(DescriptiveStats up, DescriptiveStats down, DescriptiveStats neutral,
        DescriptiveStats all, int invalidCount) {

    public static DirectionStatistics of(double[] values) {
        double[] valid = Arrays.stream(values).filter(Double::isFinite).toArray();
        return new DirectionStatistics(
                DescriptiveStats.of(Arrays.stream(valid).filter(v -> v > 0).toArray()),
                DescriptiveStats.of(Arrays.stream(valid).filter(v -> v < 0).toArray()),
                DescriptiveStats.of(Arrays.stream(valid).filter(v -> v == 0).toArray()),
                DescriptiveStats.of(valid),
                values.length - valid.length);
    }
}
