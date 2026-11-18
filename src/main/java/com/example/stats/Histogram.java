package com.example.stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Teilt Werte in gleich breite Bereiche (Balken) ein und zählt, wie viele Werte in jeden fallen. */
public final class Histogram {

    private Histogram() {
    }

    /**
     * @param values   Werte; NaN/Infinity werden ignoriert
     * @param binCount gewünschte Anzahl Balken (mind. 1)
     * @return die Balken. Liegen Werte auf beiden Seiten der 0, wird so ausgerichtet, dass 0 genau
     *         auf einer Balkengrenze liegt – damit trennt sich "aufwärts" sauber von "abwärts".
     *         Dadurch kann es einen Balken mehr als gewünscht geben.
     */
    public static List<HistogramBin> of(double[] values, int binCount) {
        double[] valid = Arrays.stream(values).filter(Double::isFinite).toArray();
        if (valid.length == 0) {
            return List.of();
        }
        double min = Arrays.stream(valid).min().getAsDouble();
        double max = Arrays.stream(valid).max().getAsDouble();
        if (min == max) {
            return List.of(new HistogramBin(min, max, valid.length));
        }

        int wanted = Math.max(1, binCount);
        double width = (max - min) / wanted;
        double start = min;
        if (min < 0 && max > 0) {
            start = Math.floor(min / width) * width; // Grenzen liegen jetzt auf Vielfachen von width, also auch auf 0
        }
        int bins = Math.max(1, (int) Math.ceil((max - start) / width));

        int[] counts = new int[bins];
        for (double v : valid) {
            int index = (int) ((v - start) / width);
            counts[Math.min(Math.max(index, 0), bins - 1)]++; // Maximum fällt in den letzten Balken
        }

        List<HistogramBin> result = new ArrayList<>(bins);
        for (int i = 0; i < bins; i++) {
            result.add(new HistogramBin(start + i * width, start + (i + 1) * width, counts[i]));
        }
        return result;
    }
}
