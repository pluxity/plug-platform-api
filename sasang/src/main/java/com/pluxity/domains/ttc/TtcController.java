package com.pluxity.domains.ttc;

import com.pluxity.domains.sse.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/ttc")
@RequiredArgsConstructor
@Slf4j
public class TtcController {

    private final TtcService ttcDataService;
    private final SseService sseService;

    @GetMapping(path = "/connect-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connectTtcStream() {
        log.info("New client requesting SSE connection for TTC data.");
        return sseService.createEmitter();
    }

    @GetMapping("/get-ttc-dummy")
    public ResponseEntity<String> pushTtcDataToClients() {
        log.info("Request received to push TTC data to all SSE clients.");

        String jsonData = ttcDataService.getParsedTtcDataAsJson();

        if (jsonData.contains("\"error\"")) {
            log.warn("Parsed data contains error, not broadcasting: {}", jsonData);
            return ResponseEntity.status(500).body("Error in parsing data, not broadcasting.");
        }

        sseService.broadcast(jsonData, "ttc-update");

        return ResponseEntity.ok("TTC data push triggered to SSE clients.");
    }
}
