package com.example.chart;

import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;

/**
 * Linienchart für berechnete Werte. Gegenstück im Browser: src/main/frontend/xau-line-chart.js
 */
@Tag("xau-line-chart")
@NpmPackage(value = "lightweight-charts", version = "5.3.0")
@JsModule("./xau-line-chart.js")
public class LineChart extends Component implements HasSize {

    public void setPoints(List<LinePoint> points) {
        getElement().setPropertyList("points", points);
    }

    /** Beschriftung der Linie (wird an der Preisachse angezeigt). */
    public void setSeriesName(String name) {
        getElement().setProperty("seriesName", name);
    }

    /** Charts mit demselben Gruppennamen zoomen und verschieben gemeinsam. Vor dem Anzeigen setzen. */
    public void setSyncGroup(String groupName) {
        getElement().setAttribute("sync-group", groupName);
    }
}
