package com.example.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.example.chart.Candle;
import com.sun.net.httpserver.HttpServer;

/** Testet Mt5Client gegen einen kleinen Fake-Server, der sich wie die Python-Bridge verhält. */
class Mt5ClientTest {

    private HttpServer server;
    private final AtomicReference<String> lastQuery = new AtomicReference<>();

    private Mt5Client startFakeBridge(int status, String body) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/candles", exchange -> {
            lastQuery.set(exchange.getRequestURI().getQuery());
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
        return new Mt5Client("http://127.0.0.1:" + server.getAddress().getPort() + "/");
    }

    @AfterEach
    void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void parsesCandles() throws IOException {
        Mt5Client client = startFakeBridge(200,
                "[{\"time\": 1731916800, \"open\": 2650.1, \"high\": 2655.0, \"low\": 2648.2, \"close\": 2652.3}]");

        List<Candle> candles = client.fetchCandles("XAUUSD", Timeframe.M15, 500);

        assertEquals(List.of(new Candle(1731916800L, 2650.1, 2655.0, 2648.2, 2652.3)), candles);
        assertEquals("symbol=XAUUSD&timeframe=M15&count=500", lastQuery.get());
    }

    @Test
    void bridgeErrorMessageIsPassedOn() throws IOException {
        Mt5Client client = startFakeBridge(404, "{\"error\": \"Symbol 'XAUUSD' gibt es bei diesem Broker nicht.\"}");

        Mt5Exception e = assertThrows(Mt5Exception.class, () -> client.fetchCandles("XAUUSD", Timeframe.H1, 10));
        assertTrue(e.getMessage().startsWith("Symbol 'XAUUSD'"), e.getMessage());
    }

    @Test
    void unreachableBridgeGivesHelpfulMessage() {
        Mt5Client client = new Mt5Client("http://127.0.0.1:1");

        Mt5Exception e = assertThrows(Mt5Exception.class, () -> client.fetchCandles("XAUUSD", Timeframe.H1, 10));
        assertTrue(e.getMessage().contains("nicht erreichbar"), e.getMessage());
    }
}
