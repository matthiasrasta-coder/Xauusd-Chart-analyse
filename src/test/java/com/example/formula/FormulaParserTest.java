package com.example.formula;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.chart.Candle;

class FormulaParserTest {

    // time, open, high, low, close
    private final List<Candle> candles = List.of(
            new Candle(0, 10, 15, 8, 12),
            new Candle(3600, 12, 14, 11, 11));

    private double[] eval(String formula) {
        return FormulaParser.parse(formula).evaluate(candles);
    }

    @Test
    void variablesAreEvaluatedPerCandle() {
        assertArrayEquals(new double[] {7, 3}, eval("High - Low"), 1e-9);
        assertArrayEquals(new double[] {12, 11}, eval("close"), 1e-9);
    }

    @Test
    void operatorPrecedenceAndParentheses() {
        assertEquals(7, eval("1 + 2 * 3")[0], 1e-9);
        assertEquals(9, eval("(1 + 2) * 3")[0], 1e-9);
        assertEquals(-4, eval("-2^2")[0], 1e-9);
        assertEquals(512, eval("2^3^2")[0], 1e-9);
        assertEquals(2.5, eval("5 / 2")[0], 1e-9);
    }

    @Test
    void functions() {
        assertEquals(2, eval("abs(Open - Close)")[0], 1e-9);
        assertEquals(3, eval("sqrt(9)")[0], 1e-9);
        assertEquals(15, eval("max(High, Close)")[0], 1e-9);
        assertEquals(8, eval("MIN(Low, Close)")[0], 1e-9);
    }

    @Test
    void divisionByZeroGivesNonFiniteValue() {
        assertTrue(Double.isInfinite(eval("High / 0")[0]));
    }

    @Test
    void errorsHaveReadableMessages() {
        assertTrue(assertThrows(FormulaException.class, () -> eval("High - Volume"))
                .getMessage().contains("Unbekannte Variable 'Volume'"));
        assertTrue(assertThrows(FormulaException.class, () -> eval("(High - Low"))
                .getMessage().contains("')' erwartet"));
        assertTrue(assertThrows(FormulaException.class, () -> eval("max(High)"))
                .getMessage().contains("erwartet 2 Argumente"));
        assertThrows(FormulaException.class, () -> eval("High +"));
        assertThrows(FormulaException.class, () -> eval("  "));
        assertThrows(FormulaException.class, () -> eval("High Low"));
    }
}
