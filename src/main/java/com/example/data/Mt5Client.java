package com.example.data;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.chart.Candle;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Holt Kerzen von der MT5-Bridge (mt5-bridge/bridge.py) per HTTP.
 * Die Bridge liest sie aus dem laufenden MetaTrader-5-Terminal.
 */
@Component
public class Mt5Client {

    /** Fehlerantwort der Bridge: {"error": "..."} */
    private record ErrorResponse(String error) {
    }

    private final String bridgeUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final JsonMapper json = JsonMapper.builder().build();

    public Mt5Client(@Value("${mt5.bridge-url}") String bridgeUrl) {
        this.bridgeUrl = bridgeUrl.replaceAll("/+$", "");
    }

    public List<Candle> fetchCandles(String symbol, Timeframe timeframe, int count) {
        URI uri = URI.create(bridgeUrl + "/candles?symbol=" + URLEncoder.encode(symbol, StandardCharsets.UTF_8)
                + "&timeframe=" + timeframe.name() + "&count=" + count);
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(30)).GET().build();

        HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new Mt5Exception("MT5-Bridge unter " + bridgeUrl + " nicht erreichbar. "
                    + "Läuft mt5-bridge/start-bridge.cmd?", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new Mt5Exception("Laden abgebrochen", e);
        }

        try {
            if (response.statusCode() != 200) {
                throw new Mt5Exception(json.readValue(response.body(), ErrorResponse.class).error());
            }
            return List.of(json.readValue(response.body(), Candle[].class));
        } catch (JacksonException e) {
            throw new Mt5Exception("Unerwartete Antwort der MT5-Bridge (HTTP " + response.statusCode() + ")", e);
        }
    }
}
