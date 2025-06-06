package com.pluxity.domains.open;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/open")
@RequiredArgsConstructor
public class OpenController {
    private final EventService eventService;

    @Operation(summary = "event 생성", description = "event를 생성하고 SSE 클라이언트에 푸시합니다.")
    @PostMapping("/events")
    public ResponseEntity<String> pushTtcDataToClients(@RequestBody EventDto eventDto) {
        eventService.makeEvent(eventDto);

        return ResponseEntity.ok("TTC data push triggered to SSE clients.");
    }
}
