package com.example.formula;

/** Fehler in einer Formel, z. B. unbekannte Variable oder fehlende Klammer. */
public class FormulaException extends RuntimeException {

    public FormulaException(String message) {
        super(message);
    }
}
