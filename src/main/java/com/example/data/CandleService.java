package com.example.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.chart.Candle;

/**
 * Zentrale Stelle, von der alle Seiten ihre Kerzen bekommen – live aus MetaTrader 5.
 * Symbol und Anzahl Kerzen stehen in application.properties (mt5.symbol, mt5.candle-count).
 */
@Service
public class CandleService {

    private final Mt5Client mt5Client;
    private final String symbol;
    private final int candleCount;

    public CandleService(Mt5Client mt5Client,
            @Value("${mt5.symbol}") String symbol,
            @Value("${mt5.candle-count}") int candleCount) {
        this.mt5Client = mt5Client;
        this.symbol = symbol;
        this.candleCount = candleCount;
    }

    public String getSymbol() {
        return symbol;
    }

    /** @throws Mt5Exception wenn die Daten nicht geladen werden konnten */
    public List<Candle> getCandles(Timeframe timeframe) {
        return mt5Client.fetchCandles(symbol, timeframe, candleCount);
    }
}
