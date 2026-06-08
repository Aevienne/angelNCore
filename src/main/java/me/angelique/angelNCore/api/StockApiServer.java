package me.angelique.angelNCore.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import me.angelique.angelNCore.AngelNCore;
import me.angelique.angelNCore.services.StockExchangeService;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class StockApiServer {

    private final AngelNCore plugin;
    private final StockExchangeService exchange;
    private HttpServer server;
    private final List<HttpExchange> sseClients = new ArrayList<>();

    public StockApiServer(AngelNCore plugin, StockExchangeService exchange) {
        this.plugin = plugin;
        this.exchange = exchange;
    }

    public void start(int port) {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/companies", this::handleCompanies);
            server.createContext("/orders/buy", this::handleBuy);
            server.createContext("/orders/sell", this::handleSell);
            server.createContext("/orders/book", this::handleOrderBook);
            server.createContext("/orders/cancel", this::handleCancelOrder);
            server.createContext("/holdings", this::handleHoldings);
            server.createContext("/price", this::handlePriceHistory);
            server.createContext("/", this::handleCors);
            server.createContext("/app", this::handleStatic);
            server.createContext("/live", this::handleLive);
            server.setExecutor(null);
            server.start();
            plugin.getLogger().info("Stock API server started on port " + port);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to start Stock API: " + e.getMessage());
        }
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    private void handleCors(HttpExchange ex) throws IOException {
        addCors(ex);
        String path = ex.getRequestURI().getPath();
        if (path.equals("/")) {
            sendJson(ex, 200, "{\"status\":\"AngelNetwork Stock Exchange API\"}");
        } else {
            sendJson(ex, 404, "{\"error\":\"not found\"}");
        }
    }

    private void handleCompanies(HttpExchange ex) throws IOException {
        addCors(ex);
        String path = ex.getRequestURI().getPath();
        String[] parts = path.split("/");
        if (parts.length > 2) {
            String cid = parts[2];
            StockExchangeService.CompanyInfo info = exchange.getCompanyInfo(cid);
            sendJson(ex, 200, toJson(info));
        } else {
            List<StockExchangeService.CompanyInfo> list = exchange.listCompanies();
            String json = "[" + list.stream().map(this::toJson).collect(Collectors.joining(",")) + "]";
            sendJson(ex, 200, json);
        }
    }

    private void handleBuy(HttpExchange ex) throws IOException {
        addCors(ex);
        if (!"POST".equals(ex.getRequestMethod())) { sendJson(ex, 405, "{}"); return; }
        Map<String, String> params = parseBody(ex);
        UUID player = UUID.fromString(params.getOrDefault("player", "00000000-0000-0000-0000-000000000000"));
        exchange.placeOrder(player, params.get("company"), "buy", Integer.parseInt(params.get("shares")), Double.parseDouble(params.get("price")));
        sendJson(ex, 200, "{\"status\":\"ok\"}");
    }

    private void handleSell(HttpExchange ex) throws IOException {
        addCors(ex);
        if (!"POST".equals(ex.getRequestMethod())) { sendJson(ex, 405, "{}"); return; }
        Map<String, String> params = parseBody(ex);
        UUID player = UUID.fromString(params.getOrDefault("player", "00000000-0000-0000-0000-000000000000"));
        exchange.placeOrder(player, params.get("company"), "sell", Integer.parseInt(params.get("shares")), Double.parseDouble(params.get("price")));
        sendJson(ex, 200, "{\"status\":\"ok\"}");
    }

    private void handleOrderBook(HttpExchange ex) throws IOException {
        addCors(ex);
        String q = ex.getRequestURI().getQuery();
        String cid = q != null && q.contains("company=") ? q.split("company=")[1] : "";
        List<StockExchangeService.OrderInfo> orders = exchange.getOrderBook(cid);
        String json = "[" + orders.stream().map(o ->
            "{\"orderId\":\"" + o.orderId() + "\",\"type\":\"" + o.type() + "\",\"shares\":" + o.shares() + ",\"price\":" + o.price() + "}"
        ).collect(Collectors.joining(",")) + "]";
        sendJson(ex, 200, json);
    }

    private void handleCancelOrder(HttpExchange ex) throws IOException {
        addCors(ex);
        if (!"POST".equals(ex.getRequestMethod())) { sendJson(ex, 405, "{}"); return; }
        Map<String, String> params = parseBody(ex);
        UUID player = UUID.fromString(params.getOrDefault("player", "00000000-0000-0000-0000-000000000000"));
        boolean ok = exchange.cancelOrder(params.get("orderId"), player);
        sendJson(ex, 200, "{\"cancelled\":" + ok + "}");
    }

    private void handleStatic(HttpExchange ex) throws IOException {
        addCors(ex);
        String path = ex.getRequestURI().getPath().replace("/app/", "index.html");
        if (path.equals("/app")) path = "index.html";
        else path = path.substring(1); // remove leading slash from /app/filename
        File webDir = new File(plugin.getDataFolder(), "web");
        File file = new File(webDir, path);
        if (!file.exists()) {
            sendJson(ex, 404, "{\"error\":\"not found\"}");
            return;
        }
        String ct = path.endsWith(".js") ? "application/javascript" : path.endsWith(".css") ? "text/css" : "text/html";
        ex.getResponseHeaders().set("Content-Type", ct);
        byte[] bytes = Files.readAllBytes(file.toPath());
        ex.sendResponseHeaders(200, bytes.length);
        OutputStream os = ex.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private void handleLive(HttpExchange ex) throws IOException {
        addCors(ex);
        ex.getResponseHeaders().set("Content-Type", "text/event-stream");
        ex.getResponseHeaders().set("Cache-Control", "no-cache");
        ex.getResponseHeaders().set("Connection", "keep-alive");
        ex.sendResponseHeaders(200, 0);
        OutputStream os = ex.getResponseBody();
        synchronized (sseClients) { sseClients.add(ex); }
        // Keep alive: send ping every 15 seconds
        new Thread(() -> {
            try {
                while (sseClients.contains(ex)) {
                    os.write(":ping\n\n".getBytes(StandardCharsets.UTF_8));
                    os.flush();
                    Thread.sleep(15000);
                }
            } catch (Exception ignored) {}
        }).start();
    }

    public void broadcastPrice(String companyId, double price) {
        String data = "data: {\"company\":\"" + companyId + "\",\"price\":" + price + "}\n\n";
        synchronized (sseClients) {
            List<HttpExchange> dead = new ArrayList<>();
            for (HttpExchange client : sseClients) {
                try {
                    OutputStream os = client.getResponseBody();
                    os.write(data.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                } catch (IOException e) {
                    dead.add(client);
                }
            }
            sseClients.removeAll(dead);
        }
    }

    private void handleHoldings(HttpExchange ex) throws IOException {
        addCors(ex);
        String q = ex.getRequestURI().getQuery();
        if (q == null || !q.contains("player=") || !q.contains("company=")) { sendJson(ex, 400, "{}"); return; }
        String[] parts = q.split("&");
        String puid = "", cid = "";
        for (String p : parts) {
            if (p.startsWith("player=")) puid = p.substring(7);
            if (p.startsWith("company=")) cid = p.substring(8);
        }
        int shares = exchange.getHolding(UUID.fromString(puid), cid);
        sendJson(ex, 200, "{\"shares\":" + shares + "}");
    }

    private void handlePriceHistory(HttpExchange ex) throws IOException {
        addCors(ex);
        String q = ex.getRequestURI().getQuery();
        String cid = q != null && q.contains("company=") ? q.split("company=")[1] : "";
        List<StockExchangeService.PriceCandle> history = exchange.getPriceHistory(cid);
        String json = "[" + history.stream().map(c ->
            "{\"t\":" + c.timestamp() + ",\"o\":" + c.open() + ",\"h\":" + c.high() + ",\"l\":" + c.low() + ",\"c\":" + c.close() + ",\"v\":" + c.volume() + "}"
        ).collect(Collectors.joining(",")) + "]";
        sendJson(ex, 200, json);
    }

    private void sendJson(HttpExchange ex, int code, String json) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, bytes.length);
        OutputStream os = ex.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private void addCors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        if ("OPTIONS".equals(ex.getRequestMethod())) {
            try { ex.sendResponseHeaders(204, -1); } catch (IOException ignored) {}
        }
    }

    private Map<String, String> parseBody(HttpExchange ex) throws IOException {
        InputStreamReader isr = new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8);
        String body = new BufferedReader(isr).lines().collect(Collectors.joining());
        Map<String, String> map = new HashMap<>();
        if (body.isEmpty()) return map;
        // Simple JSON parser for flat objects
        body = body.trim().replaceAll("[{}\"]", "");
        for (String pair : body.split(",")) {
            String[] kv = pair.split(":");
            if (kv.length == 2) map.put(kv[0].trim(), kv[1].trim());
        }
        return map;
    }

    private String toJson(StockExchangeService.CompanyInfo info) {
        if (info == null) return "{}";
        return "{\"companyId\":\"" + info.companyId() + "\",\"name\":\"" + info.name() + "\",\"totalShares\":" + info.totalShares() + ",\"currentPrice\":" + info.currentPrice() + ",\"volume\":" + info.volume() + "}";
    }
}
