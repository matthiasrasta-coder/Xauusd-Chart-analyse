import { createChart, LineSeries } from 'lightweight-charts';
import { baseOptions, joinSyncGroup, adoptGroupRange } from './chart-common.js';

/**
 * Webkomponente <xau-line-chart> für berechnete Werte (z. B. das Ergebnis einer Formel).
 * Die Java-Klasse LineChart setzt die Eigenschaft "points" mit einer Liste von
 * { time, value }. value darf null sein (z. B. Division durch 0) – dort bleibt eine Lücke.
 */
class XauLineChart extends HTMLElement {
  constructor() {
    super();
    this._points = [];
    this._seriesName = '';
  }

  connectedCallback() {
    if (this._chart) {
      return;
    }
    this.style.display = 'block';

    this._chart = createChart(this, baseOptions());
    this._series = this._chart.addSeries(LineSeries, { color: '#2962ff', lineWidth: 2 });
    this._groupName = this.getAttribute('sync-group');
    this._leaveGroup = joinSyncGroup(this._groupName, this._chart);

    this._applyData();
  }

  disconnectedCallback() {
    if (this._chart) {
      this._leaveGroup();
      this._chart.remove();
      this._chart = null;
      this._series = null;
    }
  }

  set points(value) {
    this._points = value || [];
    this._applyData();
  }

  get points() {
    return this._points;
  }

  set seriesName(value) {
    this._seriesName = value || '';
    if (this._series) {
      this._series.applyOptions({ title: this._seriesName });
    }
  }

  get seriesName() {
    return this._seriesName;
  }

  _applyData() {
    if (!this._series) {
      return;
    }
    // Punkte ohne Wert werden als "Whitespace" übergeben: Zeitstempel bleibt, Linie hat eine Lücke.
    // So bleibt die Anzahl der Punkte gleich wie beim Kerzenchart (wichtig für die Synchronisation).
    const data = this._points.map((p) => (p.value == null ? { time: p.time } : p));
    this._series.setData(data);
    this._series.applyOptions({ title: this._seriesName });

    // Gleichen Ausschnitt wie der Kerzenchart zeigen; ohne Gruppe alles anzeigen
    if (!adoptGroupRange(this._groupName, this._chart)) {
      this._chart.timeScale().fitContent();
    }
  }
}

customElements.define('xau-line-chart', XauLineChart);
