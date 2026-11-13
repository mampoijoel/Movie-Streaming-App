package com.streamdemo.player;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 * Tiny local web server that serves the trailer page (resources/player.html).
 *
 * Why? YouTube refuses to play an embedded video when the embedding page has no
 * proper web origin (it shows "Error 153" for file:// pages or inline HTML).
 * Serving the page from http://localhost gives the YouTube iframe a valid origin.
 * The server only listens on the loopback interface and serves nothing but this page.
 * No video data ever passes through it - the video streams from YouTube.
 */
public final class PlayerServer {

    private HttpServer server;
    private String template;

    public void start() throws IOException {
        try (InputStream in = PlayerServer.class.getResourceAsStream("/player.html")) {
            if (in == null) {
                throw new IOException("player.html missing from resources");
            }
            template = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        server = HttpServer.create(new InetSocketAddress(InetAddress.getByName("localhost"), 0), 0);
        server.createContext("/player", this::handle);
        server.setExecutor(Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "player-server");
            t.setDaemon(true);
            return t;
        }));
        server.start();
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    /** URL of the page that plays the given YouTube video. */
    public String urlFor(String youtubeId) {
        return "http://localhost:" + server.getAddress().getPort() + "/player?v=" + youtubeId;
    }

    private void handle(HttpExchange exchange) throws IOException {
        String id = queryParam(exchange.getRequestURI().getRawQuery(), "v");
        if (id == null || !id.matches("[A-Za-z0-9_-]{6,20}")) { // never echo unvalidated input
            exchange.sendResponseHeaders(400, -1);
            exchange.close();
            return;
        }
        byte[] body = template.replace("%VIDEO_ID%", id).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(body);
        }
    }

    private static String queryParam(String query, String name) {
        if (query == null) {
            return null;
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && pair.substring(0, eq).equals(name)) {
                return pair.substring(eq + 1);
            }
        }
        return null;
    }
}
