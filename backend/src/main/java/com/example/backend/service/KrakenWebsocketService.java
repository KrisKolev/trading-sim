package com.example.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;

@Service
public class KrakenWebsocketService {
    private static final String KRAKEN_WEBSOCKET_URL = "wss://ws.kraken.com/v2";
    private WebSocketSession session;

    //private final Map<String, TickerData>

    @PostConstruct
    public void connect() {
        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketHandler handler = new WebSocketHandler();

        client.execute(handler, KRAKEN_WEBSOCKET_URL).thenAccept(webSocketSession -> {
            this.session = webSocketSession;
            subscribeToTicker();
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
        });
    }

    private void subscribeToTicker() {
        String message = "{\"method\":\"subscribe\", \"params\": {\"channel\": \"ticker\", \"symbol\": [\"BTC/USD\", \"ETH/USD\", \"XRP/USD\", \"BCH/USD\"]}}";
        try {
            if (session != null && session.isOpen()) {
                session.sendMessage(new TextMessage(message));
            } else {
                System.err.println("WebSocket session is not open.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class WebSocketHandler extends TextWebSocketHandler {

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            System.out.println("WebSocket established");
        }

        @Override
        public void handleTextMessage(WebSocketSession session, TextMessage message) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(message.getPayload());

                if (root.has("channel") && "heartbeat".equals(root.get("channel").asText())) {
                    return;
                }

                // Check if this is a ticker update message
                if (root.has("channel") && "ticker".equals(root.get("channel").asText())) {

                    JsonNode dataArray = root.get("data");
                    if (dataArray.isArray() && dataArray.size() > 0) {
                        JsonNode tickerData = dataArray.get(0);
                        String symbol = tickerData.get("symbol").asText();
                        String fullName = mapSymbolToName(symbol);
                        String lastPrice = tickerData.get("last").asText();
                        System.out.println("Currency: " + fullName + " (" + symbol + "), Price: " + lastPrice);
                    }
                } else {
                    // For other messages, just print the payload
                    System.out.println("Received: " + message.getPayload());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String mapSymbolToName(String symbol) {
            Map<String, String> mapping = Map.ofEntries(
                Map.entry("BTC/USD", "Bitcoin"),
                Map.entry("ETH/USD", "Ethereum"),
                Map.entry("XRP/USD", "Ripple"),
                Map.entry("BCH/USD", "Bitcoin Cash"),
                Map.entry("LTC/USD", "Litecoin"),
                Map.entry("ADA/USD", "Cardano"),
                Map.entry("DOT/USD", "Polkadot"),
                Map.entry("LINK/USD", "Chainlink"),
                Map.entry("BNB/USD", "Binance Coin"),
                Map.entry("DOGE/USD", "Dogecoin"),
                Map.entry("SOL/USD", "Solana"),
                Map.entry("MATIC/USD", "Polygon"),
                Map.entry("AVAX/USD", "Avalanche"),
                Map.entry("UNI/USD", "Uniswap"),
                Map.entry("ATOM/USD", "Cosmos"),
                Map.entry("XLM/USD", "Stellar"),
                Map.entry("ICP/USD", "Internet Computer"),
                Map.entry("VET/USD", "VeChain"),
                Map.entry("ALGO/USD", "Algorand"),
                Map.entry("EOS/USD", "EOS")
            );
            return mapping.getOrDefault(symbol, symbol);
        }
    }
}
