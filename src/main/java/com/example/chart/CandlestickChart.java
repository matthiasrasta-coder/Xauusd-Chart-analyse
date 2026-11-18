package com.example.chart;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Java-Seite des Kerzencharts. Sie erzeugt im Browser das Element
 * {@code <xau-candle-chart>} (siehe src/main/frontend/xau-candle-chart.js)
 * und schickt die Kerzen als JSON dorthin.
 */
@Tag("xau-candle-chart")
@NpmPackage(value = "lightweight-charts", version = "5.3.0")
@JsModule("./xau-candle-chart.js")
public class CandlestickChart extends Component implements HasSize {

    public void setCandles(List<Candle> candles) {
        // Vaadin wandelt die Records automatisch in JSON-Objekte um
        getElement().setPropertyList("candles", candles);
    }

    /** Charts mit demselben Gruppennamen zoomen und verschieben gemeinsam. Vor dem Anzeigen setzen. */
    public void setSyncGroup(String groupName) {
        getElement().setAttribute("sync-group", groupName);
    }
}
