package com.pluxity.domains.open;

import io.swagger.v3.oas.annotations.Operation;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/open")
@RequiredArgsConstructor
public class OpenController {
    private final EventService eventService;

    @PostMapping("/events")
    @Operation(summary = "event 생성", description = "event를 생성하고 SSE 클라이언트에 푸시합니다.")
    public ResponseEntity<String> pushTtcDataToClients(@RequestBody EventDto eventDto) {
        eventService.makeEvent(eventDto);

        return ResponseEntity.ok("TTC data push triggered to SSE clients.");
    }

    @PostMapping("/shutters")
    @Operation(summary = "셔터 생성", description = "셔터를 생성하고 SSE 클라이언트에 푸시합니다.")
    public ResponseEntity<String> pushShutterDataToClients(@RequestBody ShutterDto shutterDto) {
        eventService.makeShutter(shutterDto);

        return ResponseEntity.ok("Shutter data push triggered to SSE clients.");
    }

    @PostMapping("/shutter-groups")
    @Operation(summary = "셔터 그룹 생성", description = "셔터 그룹을 생성하고 SSE 클라이언트에 푸시합니다.")
    public ResponseEntity<String> pushShutterGroupDataToClients(
            @RequestBody List<ShutterDto> shutterGroupDto) {
        eventService.makeShutterGroup(shutterGroupDto);

        return ResponseEntity.ok("Shutter group data push triggered to SSE clients.");
    }
}
