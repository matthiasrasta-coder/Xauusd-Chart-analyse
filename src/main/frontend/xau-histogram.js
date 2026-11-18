import { Chart, BarController, BarElement, CategoryScale, LinearScale, Tooltip } from 'chart.js';

// Chart.js lädt nur, was man registriert – hält das Paket klein
Chart.register(BarController, BarElement, CategoryScale, LinearScale, Tooltip);

const UP = '#26a69a';
const DOWN = '#ef5350';
const MIXED = '#9e9e9e';

const fmt = (v) => v.toLocaleString('de-DE', { maximumFractionDigits: 2 });

/**
 * Webkomponente <xau-histogram>: Balkendiagramm einer Verteilung.
 * Die Java-Klasse HistogramChart setzt "bins" mit einer Liste von { from, to, count }.
 * Balken mit positiven Werten sind grün, mit negativen rot.
 */
class XauHistogram extends HTMLElement {
  constructor() {
    super();
    this._bins = [];
  }

  connectedCallback() {
    if (this._chart) {
      return;
    }
    this.style.display = 'block';
    this.style.position = 'relative'; // Chart.js richtet sich nach der Größe dieses Elements

    const canvas = document.createElement('canvas');
    this.appendChild(canvas);

    this._chart = new Chart(canvas, {
      type: 'bar',
      data: {
        labels: [],
        datasets: [{ label: 'Anzahl', data: [], backgroundColor: [], barPercentage: 1, categoryPercentage: 1 }],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        animation: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              title: (items) => {
                const bin = this._bins[items[0].dataIndex];
                return `${fmt(bin.from)} bis ${fmt(bin.to)}`;
              },
              label: (item) => `Anzahl: ${item.raw}`,
            },
          },
        },
        scales: {
          x: { title: { display: true, text: 'Wert (Untergrenze des Balkens)' } },
          y: { beginAtZero: true, ticks: { precision: 0 }, title: { display: true, text: 'Anzahl' } },
        },
      },
    });

    this._applyData();
  }

  disconnectedCallback() {
    if (this._chart) {
      this._chart.destroy();
      this._chart = null;
      this.replaceChildren();
    }
  }

  set bins(value) {
    this._bins = value || [];
    this._applyData();
  }

  get bins() {
    return this._bins;
  }

  _applyData() {
    if (!this._chart) {
      return;
    }
    const dataset = this._chart.data.datasets[0];
    this._chart.data.labels = this._bins.map((b) => fmt(b.from));
    dataset.data = this._bins.map((b) => b.count);
    dataset.backgroundColor = this._bins.map((b) => (b.from >= 0 ? UP : b.to <= 0 ? DOWN : MIXED));
    this._chart.update();
  }
}

customElements.define('xau-histogram', XauHistogram);
