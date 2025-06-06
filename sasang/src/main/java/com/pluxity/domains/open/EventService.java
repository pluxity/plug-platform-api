package com.pluxity.domains.open;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pluxity.domains.sse.SseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

    private final SseService sseService;
    private final ObjectMapper objectMapper;

    public void makeEvent(EventDto request) {
        try {
            sseService.broadcast(objectMapper.writeValueAsString(request), "event-data");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void makeShutter(ShutterDto request) {
        try {
            sseService.broadcast(objectMapper.writeValueAsString(request), "shutter-data");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void makeShutterGroup(List<ShutterDto> request) {
        try {
            sseService.broadcast(objectMapper.writeValueAsString(request), "shutter-group-data");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
