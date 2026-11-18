package com.example.data;

/** Daten konnten nicht aus MetaTrader 5 geladen werden. Die Meldung ist für den Benutzer gedacht. */
public class Mt5Exception extends RuntimeException {

    public Mt5Exception(String message) {
        super(message);
    }

    public Mt5Exception(String message, Throwable cause) {
        super(message, cause);
    }
}
