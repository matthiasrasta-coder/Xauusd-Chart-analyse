package com.example.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class StatisticsTest {

    @Test
    void descriptiveStats() {
        DescriptiveStats s = DescriptiveStats.of(new double[] {4, 1, 3, 2});
        assertEquals(4, s.count());
        assertEquals(10, s.sum(), 1e-9);
        assertEquals(2.5, s.mean(), 1e-9);
        assertEquals(2.5, s.median(), 1e-9);
        assertEquals(Math.sqrt(5.0 / 3), s.stdDev(), 1e-9);
        assertEquals(1, s.min(), 1e-9);
        assertEquals(4, s.max(), 1e-9);

        assertEquals(3, DescriptiveStats.of(new double[] {5, 3, 1}).median(), 1e-9);
        assertEquals(0, DescriptiveStats.of(new double[0]).count());
    }

    @Test
    void directionsAreSplitBySign() {
        DirectionStatistics d = DirectionStatistics.of(new double[] {2, -1, 0, 3, Double.NaN, -4});
        assertEquals(2, d.up().count());
        assertEquals(2, d.down().count());
        assertEquals(1, d.neutral().count());
        assertEquals(5, d.all().count());
        assertEquals(1, d.invalidCount());
        assertEquals(2.5, d.up().mean(), 1e-9);
        assertEquals(-2.5, d.down().mean(), 1e-9);
    }

    @Test
    void histogramCountsAllValuesAndPutsZeroOnAnEdge() {
        double[] values = {-3.7, -1.2, -0.1, 0.4, 1.5, 2.2, 5.0};
        List<HistogramBin> bins = Histogram.of(values, 4);

        assertEquals(values.length, bins.stream().mapToInt(HistogramBin::count).sum());
        assertTrue(bins.stream().anyMatch(b -> Math.abs(b.from()) < 1e-9), "0 sollte eine Balkengrenze sein");
        assertTrue(bins.stream().noneMatch(b -> b.from() < -1e-9 && b.to() > 1e-9), "kein Balken über 0 hinweg");
        assertTrue(bins.getFirst().from() <= -3.7 && bins.getLast().to() >= 5.0);
    }

    @Test
    void histogramEdgeCases() {
        assertEquals(List.of(), Histogram.of(new double[] {Double.NaN}, 10));
        assertEquals(List.of(new HistogramBin(2, 2, 3)), Histogram.of(new double[] {2, 2, 2}, 10));
    }
}
