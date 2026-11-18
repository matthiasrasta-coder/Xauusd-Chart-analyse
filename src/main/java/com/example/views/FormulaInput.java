package com.example.views;

import java.util.function.Consumer;

import com.example.formula.Formula;
import com.example.formula.FormulaException;
import com.example.formula.FormulaParser;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Formelfeld + "Berechnen"-Button, wird von Chart- und Statistikseite verwendet.
 * Prüft die Formel und zeigt Fehler direkt am Feld an. Nur gültige Formeln werden weitergereicht.
 */
public class FormulaInput extends HorizontalLayout {

    private final TextField field = new TextField("Formel");
    private final Consumer<Formula> onCalculate;

    public FormulaInput(String initialFormula, Consumer<Formula> onCalculate) {
        this.onCalculate = onCalculate;

        field.setValue(initialFormula);
        field.setPlaceholder("z. B. High - Low");
        field.setHelperText("Variablen: Open, High, Low, Close · Operatoren: + - * / ^ ( ) · "
                + "Funktionen: abs, sqrt, log, exp, min(a,b), max(a,b)");
        field.setManualValidation(true); // Fehleranzeige steuern wir selbst
        field.setWidthFull();
        field.addKeyPressListener(Key.ENTER, e -> calculate());

        Button button = new Button("Berechnen", e -> calculate());
        button.addThemeVariants(ButtonVariant.PRIMARY);

        add(field, button);
        setWidthFull();
        setAlignItems(Alignment.BASELINE);
    }

    /** Fügt ein weiteres Eingabefeld zwischen Formelfeld und Button ein. */
    public void addBeforeButton(Component component) {
        addComponentAtIndex(1, component);
    }

    /** Liest die Formel ein und ruft bei Erfolg den Callback auf. */
    public void calculate() {
        try {
            Formula formula = FormulaParser.parse(field.getValue());
            field.setInvalid(false);
            onCalculate.accept(formula);
        } catch (FormulaException e) {
            field.setErrorMessage(e.getMessage());
            field.setInvalid(true);
        }
    }
}
