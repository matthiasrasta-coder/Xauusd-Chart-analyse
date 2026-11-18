/**
 * Gemeinsame Einstellungen und Synchronisation für alle Charts.
 */

export function baseOptions() {
  return {
    autoSize: true, // passt sich automatisch der Größe des Elements an
    layout: { background: { color: '#ffffff' }, textColor: '#333333' },
    grid: {
      vertLines: { color: '#eeeeee' },
      horzLines: { color: '#eeeeee' },
    },
    // Feste Mindestbreite der Preisachse, damit die Zeitachsen mehrerer Charts bündig sind
    rightPriceScale: { minimumWidth: 80 },
    timeScale: { timeVisible: true, secondsVisible: false },
    // Verschieben: Ziehen mit der Maus / Touch, Scrollen mit dem Mausrad (horizontal)
    handleScroll: { mouseWheel: true, pressedMouseMove: true, horzTouchDrag: true, vertTouchDrag: false },
    // Zoomen: Mausrad, Pinch-Geste, Ziehen an den Achsen
    handleScale: { mouseWheel: true, pinch: true, axisPressedMouseMove: true },
  };
}

/*
 * Charts mit demselben Gruppennamen (Attribut "sync-group") zoomen und verschieben gemeinsam.
 * Abgeglichen wird der "logische Bereich" (Index der Datenpunkte). Das funktioniert, weil alle
 * Charts einer Gruppe gleich viele Punkte mit denselben Zeitstempeln haben.
 */
const groups = new Map(); // Gruppenname -> { members: Set<chart>, syncing: boolean }

export function joinSyncGroup(groupName, chart) {
  if (!groupName) {
    return () => {};
  }
  let group = groups.get(groupName);
  if (!group) {
    group = { members: new Set(), syncing: false };
    groups.set(groupName, group);
  }
  group.members.add(chart);

  const onRangeChange = (range) => {
    if (!range || group.syncing) {
      return;
    }
    group.syncing = true;
    try {
      for (const other of group.members) {
        if (other !== chart) {
          other.timeScale().setVisibleLogicalRange(range);
        }
      }
    } finally {
      group.syncing = false;
    }
  };
  chart.timeScale().subscribeVisibleLogicalRangeChange(onRangeChange);

  // Rückgabe: Funktion zum Verlassen der Gruppe
  return () => {
    chart.timeScale().unsubscribeVisibleLogicalRangeChange(onRangeChange);
    group.members.delete(chart);
    if (group.members.size === 0) {
      groups.delete(groupName);
    }
  };
}

/** Übernimmt den sichtbaren Bereich eines anderen Charts der Gruppe. Gibt false zurück, wenn keiner da ist. */
export function adoptGroupRange(groupName, chart) {
  const group = groups.get(groupName);
  if (!group) {
    return false;
  }
  for (const other of group.members) {
    if (other !== chart) {
      const range = other.timeScale().getVisibleLogicalRange();
      if (range) {
        chart.timeScale().setVisibleLogicalRange(range);
        return true;
      }
    }
  }
  return false;
}
