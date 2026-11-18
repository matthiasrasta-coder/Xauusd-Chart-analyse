package com.example.data;

/** Zeitrahmen der Kerzen. Die Namen (M1, H1 ...) entsprechen denen in MetaTrader 5. */
public enum Timeframe {
    M1("1 Minute"),
    M5("5 Minuten"),
    M15("15 Minuten"),
    M30("30 Minuten"),
    H1("1 Stunde"),
    H4("4 Stunden"),
    D1("1 Tag");

    private final String label;

    Timeframe(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
