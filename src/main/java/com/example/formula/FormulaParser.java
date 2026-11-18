package com.example.formula;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Liest eine Formel wie {@code (High - Low) / Close * 100} ein.
 *
 * <p>Erlaubt sind:
 * <ul>
 *   <li>Variablen: Open, High, Low, Close (Groß-/Kleinschreibung egal)</li>
 *   <li>Zahlen mit Punkt als Dezimaltrenner, z. B. 0.5</li>
 *   <li>Operatoren: + - * / ^ (Potenz) und Klammern</li>
 *   <li>Funktionen: abs(x), sqrt(x), log(x) (natürlicher Logarithmus), exp(x), min(a, b), max(a, b)</li>
 * </ul>
 *
 * <p>Aufbau (von schwach nach stark bindend):
 * <pre>
 * expression := term   (('+' | '-') term)*
 * term       := unary  (('*' | '/') unary)*
 * unary      := ('-' | '+') unary | power
 * power      := primary ('^' unary)?
 * primary    := Zahl | Variable | Funktion '(' Argumente ')' | '(' expression ')'
 * </pre>
 */
public final class FormulaParser {

    private final String src;
    private int pos;

    private FormulaParser(String src) {
        this.src = src;
    }

    public static Formula parse(String text) {
        if (text == null || text.isBlank()) {
            throw new FormulaException("Bitte eine Formel eingeben, z. B. High - Low");
        }
        FormulaParser parser = new FormulaParser(text);
        Expression root = parser.parseExpression();
        parser.skipSpaces();
        if (parser.pos < parser.src.length()) {
            throw parser.error("Unerwartetes Zeichen '" + parser.src.charAt(parser.pos) + "'");
        }
        return new Formula(text.trim(), root);
    }

    private Expression parseExpression() {
        Expression left = parseTerm();
        while (true) {
            if (accept('+')) {
                Expression a = left;
                Expression b = parseTerm();
                left = (c, i) -> a.eval(c, i) + b.eval(c, i);
            } else if (accept('-')) {
                Expression a = left;
                Expression b = parseTerm();
                left = (c, i) -> a.eval(c, i) - b.eval(c, i);
            } else {
                return left;
            }
        }
    }

    private Expression parseTerm() {
        Expression left = parseUnary();
        while (true) {
            if (accept('*')) {
                Expression a = left;
                Expression b = parseUnary();
                left = (c, i) -> a.eval(c, i) * b.eval(c, i);
            } else if (accept('/')) {
                Expression a = left;
                Expression b = parseUnary();
                left = (c, i) -> a.eval(c, i) / b.eval(c, i);
            } else {
                return left;
            }
        }
    }

    private Expression parseUnary() {
        if (accept('-')) {
            Expression x = parseUnary();
            return (c, i) -> -x.eval(c, i);
        }
        if (accept('+')) {
            return parseUnary();
        }
        return parsePower();
    }

    private Expression parsePower() {
        Expression base = parsePrimary();
        if (accept('^')) {
            Expression exponent = parseUnary(); // rechtsassoziativ: 2^3^2 = 2^(3^2)
            return (c, i) -> Math.pow(base.eval(c, i), exponent.eval(c, i));
        }
        return base;
    }

    private Expression parsePrimary() {
        skipSpaces();
        if (pos >= src.length()) {
            throw error("Formel endet unerwartet");
        }
        if (accept('(')) {
            Expression inner = parseExpression();
            expect(')');
            return inner;
        }
        char ch = src.charAt(pos);
        if (Character.isDigit(ch) || ch == '.') {
            return parseNumber();
        }
        if (Character.isLetter(ch)) {
            int start = pos;
            String name = readName();
            if (accept('(')) {
                return parseFunction(name, start);
            }
            return variable(name, start);
        }
        throw error("Unerwartetes Zeichen '" + ch + "'");
    }

    private Expression parseNumber() {
        int start = pos;
        while (pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) {
            pos++;
        }
        String number = src.substring(start, pos);
        try {
            double value = Double.parseDouble(number);
            return (c, i) -> value;
        } catch (NumberFormatException e) {
            pos = start;
            throw error("Ungültige Zahl '" + number + "'");
        }
    }

    private String readName() {
        int start = pos;
        while (pos < src.length() && Character.isLetterOrDigit(src.charAt(pos))) {
            pos++;
        }
        return src.substring(start, pos);
    }

    private Expression variable(String name, int start) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "open" -> (c, i) -> c.get(i).open();
            case "high" -> (c, i) -> c.get(i).high();
            case "low" -> (c, i) -> c.get(i).low();
            case "close" -> (c, i) -> c.get(i).close();
            default -> {
                pos = start;
                throw error("Unbekannte Variable '" + name + "'. Erlaubt: Open, High, Low, Close");
            }
        };
    }

    private Expression parseFunction(String name, int start) {
        List<Expression> args = new ArrayList<>();
        if (!accept(')')) {
            do {
                args.add(parseExpression());
            } while (accept(','));
            expect(')');
        }

        String lower = name.toLowerCase(Locale.ROOT);
        switch (lower) {
            case "abs", "sqrt", "log", "exp" -> checkArgCount(name, args, 1, start);
            case "min", "max" -> checkArgCount(name, args, 2, start);
            default -> {
                pos = start;
                throw error("Unbekannte Funktion '" + name + "'. Erlaubt: abs, sqrt, log, exp, min, max");
            }
        }

        Expression a = args.get(0);
        return switch (lower) {
            case "abs" -> (c, i) -> Math.abs(a.eval(c, i));
            case "sqrt" -> (c, i) -> Math.sqrt(a.eval(c, i));
            case "log" -> (c, i) -> Math.log(a.eval(c, i));
            case "exp" -> (c, i) -> Math.exp(a.eval(c, i));
            case "min" -> {
                Expression b = args.get(1);
                yield (c, i) -> Math.min(a.eval(c, i), b.eval(c, i));
            }
            default -> { // max
                Expression b = args.get(1);
                yield (c, i) -> Math.max(a.eval(c, i), b.eval(c, i));
            }
        };
    }

    private void checkArgCount(String name, List<Expression> args, int expected, int start) {
        if (args.size() != expected) {
            pos = start;
            throw error(name + "(...) erwartet " + expected + " Argument" + (expected == 1 ? "" : "e")
                    + ", bekommen: " + args.size());
        }
    }

    // --- Hilfsmethoden zum Lesen des Textes ---

    private void skipSpaces() {
        while (pos < src.length() && Character.isWhitespace(src.charAt(pos))) {
            pos++;
        }
    }

    private boolean accept(char expected) {
        skipSpaces();
        if (pos < src.length() && src.charAt(pos) == expected) {
            pos++;
            return true;
        }
        return false;
    }

    private void expect(char expected) {
        if (!accept(expected)) {
            throw error("'" + expected + "' erwartet");
        }
    }

    private FormulaException error(String message) {
        return new FormulaException(message + " (Position " + (pos + 1) + ")");
    }
}
