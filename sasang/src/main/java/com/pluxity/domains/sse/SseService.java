package com.pluxity.domains.sse;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class SseService {
    private static final Long SSE_EMITTER_TIMEOUT = 60 * 60 * 1000L;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final AtomicInteger eventIdCounter = new AtomicInteger(0);

    private final ExecutorService sseMvcExecutor =
            Executors.newCachedThreadPool(
                    r -> {
                        Thread t = new Thread(r);
                        t.setName("sse-emitter-sender-" + t.threadId());
                        t.setDaemon(true);
                        return t;
                    });

    public SseEmitter createEmitter() {
        SseEmitter emitter = new SseEmitter(SSE_EMITTER_TIMEOUT);
        String emitterId = UUID.randomUUID().toString();

        Runnable removeEmitter =
                () -> {
                    log.info("Emitter {} removed.", emitterId);
                    this.emitters.remove(emitter);
                };

        emitter.onCompletion(removeEmitter);
        emitter.onTimeout(removeEmitter);
        emitter.onError(
                throwable -> {
                    log.error("Emitter {} error: {}", emitterId, throwable.getMessage());
                    removeEmitter.run();
                });

        this.emitters.add(emitter);
        log.info("New Emitter {} added. Total emitters: {}", emitterId, this.emitters.size());

        try {
            emitter.send(
                    SseEmitter.event().name("connection").data("SSE connection established").id(emitterId));
        } catch (IOException e) {
            log.error(
                    "Error sending initial connection event to emitter {}: {}", emitterId, e.getMessage());
            emitter.completeWithError(e);
        }

        return emitter;
    }

    public void broadcast(String jsonData, String eventName) {
        if (jsonData == null || jsonData.isEmpty()) {
            log.warn("No data to broadcast for event: {}", eventName);
            return;
        }

        String currentEventId = String.valueOf(eventIdCounter.getAndIncrement());
        log.info(
                "Broadcasting event '{}' with ID {} to {} emitters.",
                eventName,
                currentEventId,
                emitters.size());

        List<SseEmitter> deadEmitters = new ArrayList<>();

        this.emitters.forEach(
                emitter ->
                        sseMvcExecutor.execute(
                                () -> {
                                    try {
                                        emitter.send(
                                                SseEmitter.event()
                                                        .id(currentEventId)
                                                        .name(eventName)
                                                        .data(jsonData, MediaType.APPLICATION_JSON)
                                                // .reconnectTime(10000L) // 클라이언트가 재연결 시도 전 대기 시간(ms)
                                        );
                                        log.debug("Successfully sent event {} to an emitter.", currentEventId);
                                    } catch (Exception e) {
                                        log.warn(
                                                "Error sending event {} to an emitter: {}. Removing emitter.",
                                                currentEventId,
                                                e.getMessage());
                                        deadEmitters.add(emitter);
                                    }
                                }));

        if (!deadEmitters.isEmpty()) {
            this.emitters.removeAll(deadEmitters);
            log.info("Removed {} dead emitters after broadcast attempt.", deadEmitters.size());
        }
    }

    @PreDestroy
    public void shutdownExecutor() {
        if (!sseMvcExecutor.isShutdown()) {
            log.info("Shutting down SSE sender executor service.");
            sseMvcExecutor.shutdown();
        }
    }
}
