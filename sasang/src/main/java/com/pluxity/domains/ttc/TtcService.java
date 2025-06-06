package com.pluxity.domains.ttc;

import com.pluxity.domains.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TtcService {

    public static final String LINE_NUMBER = "1"; //TODO: ttcServer에 맞춰서 parse에 해당 라인 정보를 넘겨야함.
    private final SseService sseService;

    @Value("${tcp.server1}")
    private String ttcServerHost;

    @Value("${tcp.port1}")
    private int ttcServerPort;

    private final String currentHexDataStream =
            "0200143FB2001906020E020A7C015F5F1167116901011417D003"
                    + "0200133FB1001906020E020A6C028686216021620101310903"
                    + "0200133EB3001906020E020977028686215421560101227303";

    public String getParsedTtcDataAsJson() {
        log.debug("Fetching and parsing TTC data...");
        try {
            List<TtcParser.ParsedMessage> parsedMessages = TtcParser.parse(currentHexDataStream, LINE_NUMBER);

            String jsonData =
                    parsedMessages.stream()
                            .map(TtcParser.ParsedMessage::toJson)
                            .collect(Collectors.joining(",", "[", "]"));
            log.debug("Successfully parsed TTC data into JSON.");

            sseService.broadcast(jsonData, "ttc-data");
            return jsonData;
        } catch (Exception e) {
            log.error("Error parsing TTC data: {}", e.getMessage(), e);
            return String.format(
                    "{\"error\": \"Failed to process TTC data: %s\"}", e.getMessage().replace("\"", "\\\""));
        }
    }

    public String fetchAndBroadcastTtcData() {
        log.info("Attempting to fetch TTC data from TCP server {}:{}", ttcServerHost, ttcServerPort);
        String receivedHexData = null;

        try (Socket socket = new Socket(ttcServerHost, ttcServerPort)) {
            socket.setSoTimeout(3000);
            log.info("Connected to TTC TCP server.");

            InputStream inputStream = socket.getInputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            StringBuilder hexBuilder = new StringBuilder();

            long startTime = System.currentTimeMillis();
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    hexBuilder.append(String.format("%02X", buffer[i]));
                }
                if (System.currentTimeMillis() - startTime > 3000 - 500) {
                    log.warn("Approaching socket timeout, breaking read loop.");
                    break;
                }
            }
            receivedHexData = hexBuilder.toString();
            log.info("Received HEX data (length {}): {}", receivedHexData.length(), receivedHexData);

        } catch (SocketTimeoutException e) {
            log.error(
                    "Socket timeout while connecting or reading from TTC TCP server {}:{}. {}",
                    ttcServerHost,
                    ttcServerPort,
                    e.getMessage());
            return "{\"error\": \"Timeout connecting to TTC server\"}";
        } catch (IOException e) {
            log.error(
                    "IOException while communicating with TTC TCP server {}:{}. {}",
                    ttcServerHost,
                    ttcServerPort,
                    e.getMessage(),
                    e);
            return "{\"error\": \"Error communicating with TTC server: "
                    + e.getMessage().replace("\"", "\\\"")
                    + "\"}";
        }

        if (receivedHexData == null || receivedHexData.isEmpty()) {
            log.warn("No data received from TTC TCP server.");
            return "{\"error\": \"No data received from TTC server\"}";
        }

        log.debug("Parsing received TTC data...");
        try {
            List<TtcParser.ParsedMessage> parsedMessages = TtcParser.parse(receivedHexData, LINE_NUMBER);

            String jsonData =
                    parsedMessages.stream()
                            .map(TtcParser.ParsedMessage::toJson)
                            .collect(Collectors.joining(",", "[", "]"));

            log.info("Successfully parsed TTC data. Broadcasting to SSE clients...");
            sseService.broadcast(jsonData, "ttc-data");
            return jsonData;

        } catch (Exception e) {
            log.error("Error parsing TTC data or broadcasting: {}", e.getMessage(), e);
            return String.format(
                    "{\"error\": \"Failed to process TTC data: %s\"}", e.getMessage().replace("\"", "\\\""));
        }
    }
}
