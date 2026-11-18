package com.example.views;

import java.util.function.Consumer;

import com.example.data.Timeframe;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;

/**
 * Auswahl des Zeitrahmens + "Neu laden"-Button. Ruft bei Änderung bzw. Klick den Callback auf.
 */
public class TimeframeBar extends HorizontalLayout {

    private final Select<Timeframe> select = new Select<>();

    public TimeframeBar(Timeframe initial, Consumer<Timeframe> onLoad) {
        select.setLabel("Zeitrahmen");
        select.setItems(Timeframe.values());
        select.setItemLabelGenerator(tf -> tf.name() + " – " + tf.label());
        select.setValue(initial);
        select.addValueChangeListener(e -> onLoad.accept(e.getValue()));

        Button reload = new Button("Neu laden", e -> onLoad.accept(getTimeframe()));

        add(select, reload);
        setAlignItems(Alignment.BASELINE);
    }

    public Timeframe getTimeframe() {
        return select.getValue();
    }
}
