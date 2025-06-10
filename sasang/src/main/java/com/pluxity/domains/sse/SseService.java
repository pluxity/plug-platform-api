package com.pluxity.domains.sse;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@Slf4j
public class SseService {

    // 1. 타임아웃을 적절히 설정 (너무 길면 죽은 커넥션이 오래 남을 수 있음)
    private static final Long DEFAULT_TIMEOUT = 30 * 60 * 1000L; // 30분

    // 2. Emitter를 ID 기반으로 관리하여 확장성 확보. 동시성 이슈를 위해 ConcurrentHashMap 사용.
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final AtomicLong eventIdCounter = new AtomicLong();

    public SseEmitter createEmitter() {
        String clientId = UUID.randomUUID().toString();
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        // 4. Emitter 제거 로직을 콜백에 완전히 위임. 이것이 가장 안전한 방법.
        // onCompletion, onTimeout, onError 콜백은 Spring이 관리하는 스레드에서 안전하게 실행됨.
        Runnable removeEmitter =
                () -> {
                    log.info("Emitter for client {} has completed. Removing from map.", clientId);
                    this.emitters.remove(clientId);
                };

        emitter.onCompletion(removeEmitter);
        emitter.onTimeout(removeEmitter);
        emitter.onError(
                throwable -> {
                    log.error(
                            "Emitter for client {} encountered an error: {}", clientId, throwable.getMessage());
                    // onError 콜백은 자동으로 complete를 호출하지 않으므로, 여기서 명시적으로 제거 로직을 실행.
                    removeEmitter.run();
                });

        this.emitters.put(clientId, emitter);
        log.info(
                "New Emitter created for client {}. Total emitters: {}", clientId, this.emitters.size());

        // 5. 최초 연결 시, 클라이언트 식별을 위한 더미 이벤트 전송
        sendToClient(clientId, "connect", "Connection established. Client ID: " + clientId);

        return emitter;
    }

    // 6. broadcast 메소드 단순화 및 안전성 확보
    public void broadcast(String jsonData, String eventName) {
        if (jsonData == null || jsonData.isEmpty()) {
            log.warn("No data to broadcast for event: {}", eventName);
            return;
        }

        log.info("Broadcasting event '{}' to {} emitters.", eventName, emitters.size());

        // Map의 모든 Emitter에 대해 sendToClient를 호출
        emitters.forEach((clientId, emitter) -> sendToClient(clientId, eventName, jsonData));
    }

    public void sendToClient(String clientId, String eventName, String data) {
        SseEmitter emitter = emitters.get(clientId);
        if (emitter == null) {
            log.warn("No emitter found for client ID: {}", clientId);
            return;
        }

        try {
            // SseEmitter.send는 내부적으로 비동기 처리됨. 별도의 스레드 풀은 불필요.
            emitter.send(
                    SseEmitter.event()
                            .id(String.valueOf(eventIdCounter.getAndIncrement()))
                            .name(eventName)
                            .data(data, MediaType.APPLICATION_JSON));
            log.debug("Successfully sent event '{}' to client {}", eventName, clientId);
        } catch (Exception e) {
            // send 도중 예외 발생 시(네트워크 단절 등), onError 콜백이 트리거됨.
            // 따라서 여기서 직접 Emitter를 제거할 필요 없이, 콜백에 맡기는 것이 일관성 있음.
            log.warn("Failed to send event to client {}: {}", clientId, e.getMessage());
            // emitter.completeWithError(e); // onError 콜백을 직접 트리거하고 싶다면 호출 가능.
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down SSE service...");
        // 모든 연결을 종료
        emitters.forEach((id, emitter) -> emitter.complete());
        emitters.clear();
    }
}
