package com.pluxity.domains.ttc;

import com.pluxity.domains.sse.SseService;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TtcService {

    public static final String LINE_NUMBER = "1";
    private final SseService sseService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    @Value("${tcp.server1}")
    private String ttcServerHost;

    @Value("${tcp.port1}")
    private int ttcServerPort;

    @Value("${tcp.server2}")
    private String ttcServerHost2;

    @Value("${tcp.port2}")
    private int ttcServerPort2;

    @Value("${tcp.server3}")
    private String ttcServerHost3;

    @Value("${tcp.port3}")
    private int ttcServerPort3;

    @Value("${tcp.server4}")
    private String ttcServerHost4;

    @Value("${tcp.port4}")
    private int ttcServerPort4;

    private final String currentHexDataStream =
            "0200143FB2001906020E020A7C015F5F1167116901011417D003"
                    + "0200133FB1001906020E020A6C028686216021620101310903"
                    + "0200133EB3001906020E020977028686215421560101227303";

    @EventListener(ApplicationReadyEvent.class)
    public void initializeTtcConnections() {
        isRunning.set(true);
        CompletableFuture.runAsync(
                () -> maintainConnection("1호선", ttcServerHost, ttcServerPort), executorService);
        CompletableFuture.runAsync(
                () -> maintainConnection("2호선", ttcServerHost2, ttcServerPort2), executorService);
        CompletableFuture.runAsync(
                () -> maintainConnection("3호선", ttcServerHost3, ttcServerPort3), executorService);
        CompletableFuture.runAsync(
                () -> maintainConnection("4호선", ttcServerHost4, ttcServerPort4), executorService);
    }

    private void maintainConnection(String serverName, String host, int port) {
        while (isRunning.get()) {
            Socket socket = null;
            try {
                log.info("Connecting to {}:{}", host, port);
                socket = new Socket(host, port);
                socket.setSoTimeout(5000);

                processDataStream(socket, serverName);
            } catch (Exception e) {
                log.error("Failed to connect to TTC {}: {}", serverName, e.getMessage());
            } finally {
                closeSocket(socket, serverName);
                sleepBeforeReconnect();
            }
        }
    }

    private void processDataStream(Socket socket, String serverName) throws IOException {
        InputStream inputStream = socket.getInputStream();
        byte[] buffer = new byte[4096];
        while (isRunning.get() && !socket.isClosed()) {
            try {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    log.error("Connection closed by TTC {}", serverName);
                    break;
                }
                if (bytesRead > 0) {
                    String hexData = convertBytesToHex(buffer, bytesRead);
                    processReceivedData(hexData, serverName);
                }
            } catch (SocketTimeoutException e) {
                // 타임아웃은 정상적인 상황, 계속 읽기 시도
                continue;
            } catch (IOException e) {
                log.error("Error reading data from TTC {}: {}", serverName, e.getMessage());
                break;
            }
        }
    }

    private void closeSocket(Socket socket, String serverName) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                log.error("Error closing socket for TTC {}: {}", serverName, e.getMessage());
            }
        }
    }

    private void sleepBeforeReconnect() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String convertBytesToHex(byte[] buffer, int length) {
        StringBuilder hexBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            hexBuilder.append(String.format("%02X", buffer[i]));
        }
        return hexBuilder.toString();
    }

    private void processReceivedData(String hexData, String serverName) {
        if (hexData.isEmpty()) {
            return;
        }
        try {
            List<TtcParser.ParsedMessage> parsedMessages = TtcParser.parse(hexData, LINE_NUMBER);
            String jsonData = convertToJson(parsedMessages);
            sseService.broadcast(jsonData, "ttc-data");
            log.info("Received data from TTC {}: {}", serverName, jsonData);
        } catch (Exception e) {
            log.error("Error processing data from {}: {}", serverName, e.getMessage());
        }
    }

    private String convertToJson(List<TtcParser.ParsedMessage> parsedMessages) {
        return parsedMessages.stream()
                .map(TtcParser.ParsedMessage::toJson)
                .collect(Collectors.joining(",", "[", "]"));
    }

    public String getParsedTtcDataAsJson() {
        try {
            List<TtcParser.ParsedMessage> parsedMessages =
                    TtcParser.parse(currentHexDataStream, LINE_NUMBER);

            String jsonData = convertToJson(parsedMessages);
            sseService.broadcast(jsonData, "ttc-data");
            return jsonData;
        } catch (Exception e) {
            log.error("Error parsing TTC data: {}", e.getMessage());
            return createErrorResponse("Failed to process TTC data: " + e.getMessage());
        }
    }

    public String createTtcData(Long id) {
        if (id == null || id < 1 || id > 6) {
            return "{\"error\": \"Invalid id. Must be between 1 and 6\"}";
        }

        try {
            String hexData = getHexDataById(id);
            List<TtcParser.ParsedMessage> parsedMessages = TtcParser.parse(hexData, LINE_NUMBER);
            String jsonData = convertToJson(parsedMessages);

            sseService.broadcast(jsonData, "ttc-data");
            return jsonData;

        } catch (Exception e) {
            log.error("Error creating TTC data for id {}: {}", id, e.getMessage());
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

    private String createErrorResponse(String message) {
        return String.format("{\"error\": \"%s\"}", message.replace("\"", "\\\""));
    }
}
