package com.pluxity.domains.ttc;

import com.pluxity.domains.sse.SseService;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TtcService {

    public static final String LINE_NUMBER = "1"; // TODO: ttcServer에 맞춰서 parse에 해당 라인 정보를 넘겨야함.
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
            List<TtcParser.ParsedMessage> parsedMessages =
                    TtcParser.parse(currentHexDataStream, LINE_NUMBER);

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

    public String createTtcData(Long id) {
        if (id == null || id < 1 || id > 6) {
            String errorMessage = "{\"error\": \"Invalid id. Must be between 1 and 6\"}";
            log.warn("Invalid TTC data creation request with id: {}", id);
            return errorMessage;
        }

        String hexData = getHexDataById(id);
        log.debug("Creating TTC data for id: {} with hex length: {}", id, hexData.length());

        try {
            List<TtcParser.ParsedMessage> parsedMessages = TtcParser.parse(hexData, LINE_NUMBER);

            String jsonData =
                    parsedMessages.stream()
                            .map(TtcParser.ParsedMessage::toJson)
                            .collect(Collectors.joining(",", "[", "]"));

            log.info("Successfully created TTC data for id: {}. Broadcasting to SSE clients...", id);
            sseService.broadcast(jsonData, "ttc-data");
            return jsonData;

        } catch (Exception e) {
            log.error("Error creating TTC data for id {}: {}", id, e.getMessage(), e);
            return String.format(
                    "{\"error\": \"Failed to create TTC data for id %d: %s\"}",
                    id, e.getMessage().replace("\"", "\\\""));
        }
    }

    private String getHexDataById(Long id) {
        return switch (id.intValue()) {
            case 1 -> "020013" + "01B1" + "0018050A0B0C0D77018686216621680101AABB03";
            case 2 -> "020013" + "02B1" + "0018050A0B0C0E77028787216721690101CCDD03";
            case 3 -> "020014" + "03B2" + "0018050A0B0C0F770188882168216A01010EEEFF03";
            case 4 -> "020014" + "04B2" + "0018050A0B0C10770289892169216B01011FA1B203";
            case 5 -> "020013" + "05B3" + "0018050A0B0C1177018A8A216A216C0101C3D403";
            case 6 -> "020013" + "06B3" + "0018050A0B0C1277028B8B216B216D0101E5F603";
            default -> throw new IllegalArgumentException("Invalid id: " + id);
        };
    }
}
