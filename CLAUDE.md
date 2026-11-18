# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Stack

Spring Boot 4.1 + Vaadin Flow 25.3 (server-side Java UI, no hand-written HTML/JS), Java 25, Maven (wrapper included). Market data comes live from MetaTrader 5 through a small Python bridge (`mt5-bridge/`).

## Commands

On Windows use `mvnw.cmd` instead of `./mvnw`.

- Run in dev mode: `./mvnw spring-boot:run` (also the default goal, so plain `./mvnw` works). Serves on http://localhost:8080 (override with `PORT` env var or `server.port`).
- Production build: `./mvnw package` then `java -jar target/*.jar`. The `vaadin-maven-plugin` `build-frontend` goal runs during package.
- Tests: `./mvnw test`; single test: `./mvnw test -Dtest=ClassName#method`. Tests don't need MT5 (`Mt5ClientTest` uses a fake JDK `HttpServer`). `browserless-test-spring` is on the test classpath for testing Vaadin views without a browser.
- MT5 bridge (must run for the app to show data): `mt5-bridge\start-bridge.cmd` — creates `mt5-bridge/.venv` on first run, installs `MetaTrader5` from `requirements.txt`, serves on `127.0.0.1:5005`. Needs the MT5 terminal running and logged in (it is found automatically; else set `MT5_PATH`). Check with `curl http://127.0.0.1:5005/health`.
- Docker: `docker build -t my-application:latest .` (pass a Vaadin Pro key with `--secret id=proKey,src=$HOME/.vaadin/proKey` if commercial components are used).

## Vaadin Dev Loop (agentic development)

If `.vaadin/` exists (installed via `./mvnw vaadin:install-dev-cli`), start and manage the app with `.vaadin/vaadin-dev start|status|restart` (Windows: `.vaadin\vaadin-dev.cmd`) instead of `spring-boot:run`. Only one launcher may run at a time — the Dev Loop, `spring-boot:run`, and IDE launches all compete for port 8080. The installer also adds skills under `.claude/skills/`; follow them when present.

## Architecture

- `Application` — Spring Boot entry point.
- `AppShell` (`AppShellConfigurator`) — app-wide page config: loads the Aura theme stylesheet plus `src/main/resources/META-INF/resources/styles.css` via `@StyleSheet`, and enables server push (`@Push`). Page-level annotations like `@StyleSheet`, `@Push`, `@Viewport` belong here, not on views.
- Views are Java classes annotated with `@Route` under `com.example`. `vaadin.allowed-packages` in `application.properties` restricts classpath scanning to `com.vaadin, org.vaadin, com.flowingcode, com.example` — put new code under `com.example` (or add the package there) or routes won't be discovered.
- Chart: `chart/CandlestickChart` (Java, `@Tag("xau-candle-chart")` + `@NpmPackage` lightweight-charts 5.x + `@JsModule`) wraps the plain web component in `src/main/frontend/xau-candle-chart.js`. Data goes Java→JS via `getElement().setPropertyList("candles", List<Candle>)` (Jackson 3 serializes the `Candle` record; `time` = UNIX seconds as delivered by MT5, i.e. broker server time, so the axis matches the MT5 terminal). Lightweight Charts v5 API: `chart.addSeries(CandlestickSeries, …)`. `views/ChartView` is the `""` route.
- Data flow: views → `data/CandleService.getCandles(Timeframe)` → `data/Mt5Client` (JDK `HttpClient` + Jackson 3 `JsonMapper`, package `tools.jackson`) → `GET {mt5.bridge-url}/candles?symbol=&timeframe=&count=` → `mt5-bridge/bridge.py` (`MetaTrader5.copy_rates_from_pos`; last candle is the still-forming one). Bridge errors come back as `{"error": "..."}` and surface as `Mt5Exception` → error notification in the view. The bridge is single-threaded on purpose (the MetaTrader5 library isn't thread-safe). Config: `mt5.*` in `application.properties` (symbol name is broker-specific). `Timeframe` enum names must match the bridge's `TIMEFRAMES` keys.
- `LineChart` / `xau-line-chart.js` shows computed series (`LinePoint`, `value == null` → whitespace gap). Shared chart options and zoom/pan sync live in `src/main/frontend/chart-common.js`: charts with the same `sync-group` attribute share the visible *logical* range, so every chart in a group must have one point per candle (same count, same times).
- Formulas (`com.example.formula`): hand-written recursive-descent `FormulaParser` → `Formula.evaluate(List<Candle>)` → `double[]`, one value per candle. `Expression.eval(candles, index)` takes the whole list + index so look-back features (e.g. `Close[1]`, moving averages) can be added. Error messages are German and user-facing. Tests: `FormulaParserTest`.
- Views use `MainLayout` (AppLayout with nav links) via `@Route(..., layout = MainLayout.class)`. `CandleService` is constructor-injected into views and `MainLayout`. Shared view parts: `FormulaInput` (formula field + button), `TimeframeBar` (timeframe select + reload), `Notifications.showError`. Each view keeps its own candle list and reloads it on timeframe change, then re-runs `formulaInput.calculate()`.
- Statistics page (`StatisticsView`, route `statistik`): formula value per candle → `stats/DirectionStatistics` (split by sign) + `stats/Histogram` (bins aligned so 0 is an edge) → Vaadin `Grid` + `HistogramChart` (Chart.js 4.x, `xau-histogram.js`; only the needed Chart.js parts are registered). Lightweight Charts is time-axis only, so non-time charts use Chart.js.
- `src/main/bundles/` is the Vaadin-generated dev bundle (regenerated when frontend deps change).
- The `vaadin` artifact includes commercial components; swap to `vaadin-core` for free components only.

## Vaadin API lookups

The Vaadin MCP server is configured in `.mcp.json`. Vaadin 25.x APIs post-date much training data — use its tools (`get_vaadin_primer`, `get_new_apis`, `get_java_symbol`, `search_vaadin_docs` with version 25.3, ui_language java) before writing or assuming component APIs.

## Projektziel
Kerzenchart für XAUUSD mit Zoom und Verschieben, gebaut mit Vaadin Flow.

## Hinweise
Ich bin Einsteiger. Bitte erkläre kurz, was du änderst und warum.
Arbeite in kleinen Schritten.