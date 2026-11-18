package com.example.chart;

import java.util.List;

import com.example.stats.HistogramBin;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Histogramm (Balkendiagramm einer Verteilung) mit Chart.js.
 * Gegenstück im Browser: src/main/frontend/xau-histogram.js
 */
@Tag("xau-histogram")
@NpmPackage(value = "chart.js", version = "4.6.1")
@JsModule("./xau-histogram.js")
public class HistogramChart extends Component implements HasSize {

    public void setBins(List<HistogramBin> bins) {
        getElement().setPropertyList("bins", bins);
    }
}
