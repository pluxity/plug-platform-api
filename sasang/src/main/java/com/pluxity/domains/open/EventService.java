package com.pluxity.domains.open;

import com.pluxity.domains.sse.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

    private final SseService sseService;

    public void makeEvent(EventDto request) {
        sseService.broadcast(request.toString(), "event-data");
    }
}
