package com.example.backend.service;

import com.example.backend.model.TickerData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@EnableScheduling
public class KrakenWebsocketService {
    private static final String KRAKEN_WEBSOCKET_URL = "wss://ws.kraken.com/v2";
    private WebSocketSession session;

    //Store ticker data keyed by symbol
    private final Map<String, TickerData> tickerDataMap = new ConcurrentHashMap<>();

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
        //Defining currency pairs
        List<String> pairs = List.of(
                "BTC/USD", "ETH/USD", "XRP/USD", "BCH/USD", "LTC/USD",
                "ADA/USD", "DOT/USD", "LINK/USD", "DOGE/USD"
                // Add more if supported
        );
        String pairsJson = new ObjectMapper().valueToTree(pairs).toString();
        String message = "{\"method\":\"subscribe\", \"params\": {\"channel\": \"ticker\", \"symbol\": " + pairsJson + "}}";
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

    //Updates prices every 20 secs becuase it is not needed to update so fast
    @Scheduled(fixedRate = 20000)
    public void sendConsolidatedTickerData() {
        System.out.println("===== Consolidated Ticker Data Update =====");
        if (tickerDataMap.isEmpty()) {
            System.out.println("No ticker data available yet.");
        } else {
            //Print each symbol's latest price.
            tickerDataMap.forEach((symbol, data) -> {
                String fullName = mapSymbolToName(symbol);
                System.out.println("Currency: " + fullName + " (" + symbol + "), Price: " + data.getLastPrice());
            });
        }
        System.out.println("===========================================");
    }

    // The WebSocket handler updates the tickerDataMap without logging each update.
    private class WebSocketHandler extends TextWebSocketHandler {
        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            System.out.println("WebSocket established");
        }

        @Override
        public void handleTextMessage(WebSocketSession session, TextMessage message) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(message.getPayload());

                // Skip heartbeat messages
                if (root.has("channel") && "heartbeat".equals(root.get("channel").asText())) {
                    return;
                }

                // Process ticker messages (either snapshot or update)
                if (root.has("channel") && "ticker".equals(root.get("channel").asText())) {
                    String type = root.has("type") ? root.get("type").asText() : "";
                    if ("snapshot".equals(type) || "update".equals(type)) {
                        JsonNode dataArray = root.get("data");
                        if (dataArray != null && dataArray.isArray() && dataArray.size() > 0) {
                            JsonNode tickerData = dataArray.get(0);
                            if (tickerData.has("symbol") && tickerData.has("last")) {
                                String symbol = tickerData.get("symbol").asText();
                                String lastPrice = tickerData.get("last").asText();
                                // Update the shared ticker data map
                                tickerDataMap.put(symbol, new TickerData(lastPrice));
                            }
                        }
                    }
                }
                // Optionally, you can log or process other message types as needed.
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Map symbol to full cryptocurrency name
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
                Map.entry("DOGE/USD", "Dogecoin")
        );
        return mapping.getOrDefault(symbol, symbol);
    }
}
