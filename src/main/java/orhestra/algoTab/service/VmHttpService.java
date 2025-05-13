package orhestra.algoTab.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import orhestra.algoTab.model.OptimizationTask;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class VmHttpService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static String pingVM(String ip) {
        try {
            HttpURLConnection connection = createConnection("http://" + ip + ":8080/ping", "GET");
            connection.setConnectTimeout(1500);
            connection.setReadTimeout(1500);

            int code = connection.getResponseCode();
            return switch (code) {
                case 200 -> "✅ Доступен (200 OK)";
                case 404 -> "⚠️ Нет /ping (404)";
                default -> "⚠️ Код " + code;
            };
        } catch (Exception e) {
            return "❌ Нет соединения";
        }
    }

    public static String uploadJarToVm(String ip, File jarFile) throws IOException {
        HttpURLConnection conn = createConnection("http://" + ip + ":8080/upload", "POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/octet-stream");

        try (OutputStream out = conn.getOutputStream(); FileInputStream fis = new FileInputStream(jarFile)) {
            fis.transferTo(out);
        }

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new IOException("Upload failed: HTTP " + code);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            Map<?, ?> json = objectMapper.readValue(reader, Map.class);
            return (String) json.get("path");
        }
    }

    public static void sendTaskToVm(String ip, String jarPath, OptimizationTask task,
                                    Consumer<Map<String, Object>> onResponse,
                                    Consumer<Exception> onError) {
        executor.submit(() -> {
            long start = System.nanoTime();
            try {
                HttpURLConnection conn = createConnection("http://" + ip + ":8080/run", "POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(3000);
                conn.setRequestProperty("Content-Type", "application/json");

                Map<String, Object> request = Map.of(
                        "jarPath", jarPath,
                        "nAgents", task.getnAgents(),
                        "dim", task.getDim(),
                        "nIterations", task.getnIterations(),
                        "function", task.getFunction()
                );

                try (OutputStream os = conn.getOutputStream()) {
                    objectMapper.writeValue(os, request);
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    Map<String, Object> response = objectMapper.readValue(reader, Map.class);
                    double durationSec = (System.nanoTime() - start) / 1_000_000_000.0;

                    response.put("duration", String.format("%.2f", durationSec));
                    response.put("taskNumber", task.getTaskNumber());
                    response.put("ip", ip);

                    onResponse.accept(response);
                }

            } catch (Exception e) {
                onError.accept(e);
            }
        });
    }

    private static HttpURLConnection createConnection(String url, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        return conn;
    }
}


