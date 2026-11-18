"""
MT5-Bridge: liest Kerzen aus dem laufenden MetaTrader-5-Terminal und stellt sie per HTTP bereit.

Voraussetzungen:
  - MetaTrader 5 ist auf diesem PC installiert, gestartet und bei einem Konto angemeldet.
  - Python-Paket "MetaTrader5" (siehe requirements.txt; start-bridge.cmd installiert es automatisch).

Endpunkte (nur lokal erreichbar unter http://127.0.0.1:5005):
  GET /health
      -> {"connected": true, "company": "...", "server": "...", "login": 123}
  GET /candles?symbol=XAUUSD&timeframe=H1&count=2000
      -> [{"time": 1731916800, "open": ..., "high": ..., "low": ..., "close": ...}, ...]
      time = Startzeit der Kerze in Sekunden, in der Serverzeit des Brokers (wie im MT5-Chart).
      Die letzte Kerze ist die aktuell laufende (noch nicht abgeschlossene).
  Fehler -> HTTP 4xx/5xx mit {"error": "Meldung"}

Optionale Umgebungsvariablen:
  MT5_PATH          Pfad zur terminal64.exe, falls MT5 nicht automatisch gefunden wird
  MT5_BRIDGE_PORT   anderer Port als 5005
"""

import json
import os
from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import parse_qs, urlparse

import MetaTrader5 as mt5

HOST = "127.0.0.1"  # nur lokal erreichbar, nicht aus dem Netzwerk
PORT = int(os.environ.get("MT5_BRIDGE_PORT", "5005"))
MAX_COUNT = 50_000

TIMEFRAMES = {
    "M1": mt5.TIMEFRAME_M1,
    "M5": mt5.TIMEFRAME_M5,
    "M15": mt5.TIMEFRAME_M15,
    "M30": mt5.TIMEFRAME_M30,
    "H1": mt5.TIMEFRAME_H1,
    "H4": mt5.TIMEFRAME_H4,
    "D1": mt5.TIMEFRAME_D1,
}


class BridgeError(Exception):
    def __init__(self, status, message):
        super().__init__(message)
        self.status = status


def ensure_connected():
    """Verbindet sich mit dem Terminal, falls noch nicht (oder nicht mehr) verbunden."""
    if mt5.terminal_info() is not None:
        return
    path = os.environ.get("MT5_PATH")
    ok = mt5.initialize(path) if path else mt5.initialize()
    if not ok:
        raise BridgeError(
            503,
            f"Keine Verbindung zu MetaTrader 5 ({mt5.last_error()}). "
            "Läuft das Terminal und ist ein Konto angemeldet? "
            "Falls MT5 nicht gefunden wird: Umgebungsvariable MT5_PATH auf terminal64.exe setzen.",
        )


def health():
    ensure_connected()
    terminal = mt5.terminal_info()
    account = mt5.account_info()
    return {
        "connected": True,
        "company": terminal.company if terminal else None,
        "server": account.server if account else None,
        "login": account.login if account else None,
    }


def get_candles(symbol, timeframe, count):
    ensure_connected()

    if mt5.symbol_info(symbol) is None:
        similar = [s.name for s in (mt5.symbols_get(group=f"*{symbol[:3]}*") or [])][:15]
        raise BridgeError(
            404,
            f"Symbol '{symbol}' gibt es bei diesem Broker nicht. "
            f"Ähnliche Symbole: {', '.join(similar) if similar else 'keine gefunden'}",
        )
    # Symbol in die Marktübersicht aufnehmen, sonst liefert MT5 evtl. keine Daten
    mt5.symbol_select(symbol, True)

    rates = mt5.copy_rates_from_pos(symbol, TIMEFRAMES[timeframe], 0, count)
    if rates is None or len(rates) == 0:
        raise BridgeError(502, f"MT5 hat keine Kursdaten geliefert ({mt5.last_error()}). "
                               "Evtl. lädt MT5 die Historie gerade noch – in ein paar Sekunden erneut versuchen.")

    return [
        {
            "time": int(r["time"]),
            "open": float(r["open"]),
            "high": float(r["high"]),
            "low": float(r["low"]),
            "close": float(r["close"]),
        }
        for r in rates
    ]


def parse_candle_params(query):
    symbol = query.get("symbol", ["XAUUSD"])[0].strip()
    timeframe = query.get("timeframe", ["H1"])[0].strip().upper()
    if timeframe not in TIMEFRAMES:
        raise BridgeError(400, f"Unbekannter Zeitrahmen '{timeframe}'. Erlaubt: {', '.join(TIMEFRAMES)}")
    try:
        count = int(query.get("count", ["1000"])[0])
    except ValueError:
        raise BridgeError(400, "Parameter 'count' muss eine Zahl sein")
    if not 1 <= count <= MAX_COUNT:
        raise BridgeError(400, f"Parameter 'count' muss zwischen 1 und {MAX_COUNT} liegen")
    return symbol, timeframe, count


class Handler(BaseHTTPRequestHandler):
    def do_GET(self):
        url = urlparse(self.path)
        try:
            if url.path == "/health":
                self.send_json(200, health())
            elif url.path == "/candles":
                self.send_json(200, get_candles(*parse_candle_params(parse_qs(url.query))))
            else:
                raise BridgeError(404, f"Unbekannter Pfad {url.path}. Verfügbar: /health, /candles")
        except BridgeError as e:
            self.send_json(e.status, {"error": str(e)})
        except Exception as e:  # unerwarteter Fehler: trotzdem verständliche Antwort schicken
            self.send_json(500, {"error": f"Interner Fehler in der MT5-Bridge: {e!r}"})

    def send_json(self, status, payload):
        body = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, fmt, *args):
        print(f"[bridge] {self.address_string()} {fmt % args}")


def main():
    # Die MetaTrader5-Bibliothek ist nicht thread-sicher – daher bewusst ein Server ohne Threads.
    server = HTTPServer((HOST, PORT), Handler)
    print(f"MT5-Bridge läuft auf http://{HOST}:{PORT}  (Beenden mit Strg+C)")
    try:
        ensure_connected()
        print("Verbunden mit MetaTrader 5:", health())
    except BridgeError as e:
        print("Hinweis:", e, "– neuer Versuch bei der nächsten Anfrage.")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        mt5.shutdown()


if __name__ == "__main__":
    main()
