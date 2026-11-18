import { createChart, CandlestickSeries } from 'lightweight-charts';
import { baseOptions, joinSyncGroup } from './chart-common.js';

/**
 * Webkomponente <xau-candle-chart>, die einen Lightweight-Charts-Kerzenchart anzeigt.
 * Die Java-Klasse CandlestickChart setzt die Eigenschaft "candles" mit einer Liste von
 * { time, open, high, low, close } (time = UNIX-Sekunden, UTC).
 */
class XauCandleChart extends HTMLElement {
  constructor() {
    super();
    this._candles = [];
  }

  connectedCallback() {
    if (this._chart) {
      return;
    }
    this.style.display = 'block';

    this._chart = createChart(this, baseOptions());
    this._series = this._chart.addSeries(CandlestickSeries, {
      upColor: '#26a69a',
      downColor: '#ef5350',
      borderVisible: false,
      wickUpColor: '#26a69a',
      wickDownColor: '#ef5350',
    });
    this._leaveGroup = joinSyncGroup(this.getAttribute('sync-group'), this._chart);

    this._applyData();
  }

  disconnectedCallback() {
    // Vaadin kann das Element entfernen und später wieder einhängen – dann neu aufbauen
    if (this._chart) {
      this._leaveGroup();
      this._chart.remove();
      this._chart = null;
      this._series = null;
    }
  }

  set candles(value) {
    this._candles = value || [];
    this._applyData();
  }

  get candles() {
    return this._candles;
  }

  _applyData() {
    if (this._series) {
      this._series.setData(this._candles);
      this._chart.timeScale().fitContent();
    }
  }
}

customElements.define('xau-candle-chart', XauCandleChart);
