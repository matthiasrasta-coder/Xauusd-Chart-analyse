package com.example.chart;

/**
 * Eine Kerze. {@code time} ist der Startzeitpunkt in UNIX-Sekunden (UTC),
 * so wie Lightweight Charts ihn erwartet.
 */
public record Candle(long time, double open, double high, double low, double close) {
}
