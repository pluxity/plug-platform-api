package com.pluxity.domains.ttc;

import com.pluxity.domains.sse.SseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ttc")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ttc Controller", description = "TTC와 관련된건데 정의된게 거의 없어서 추상적으로 적음.")
public class TtcController {

    private final TtcService ttcDataService;
    private final SseService sseService;

    @Operation(summary = "ttc 더미 데이터 생성", description = "ttc 더미 데이터를 생성하고 SSE 클라이언트에 푸시합니다.")
    @GetMapping("/get-ttc-dummy")
    public ResponseEntity<String> pushTtcDataToClients() {
        log.info("Request received to push TTC data to all SSE clients.");

        String jsonData = ttcDataService.getParsedTtcDataAsJson();

        if (jsonData.contains("\"error\"")) {
            log.warn("Parsed data contains error, not broadcasting: {}", jsonData);
            return ResponseEntity.status(500).body("Error in parsing data, not broadcasting.");
        }

        return ResponseEntity.ok("TTC data push triggered to SSE clients.");
    }

    @GetMapping("/tcp")
    @Operation(summary = "TCP 연결 테스트", description = "TCP 연결을 테스트합니다.")
    public ResponseEntity<String> testTcpConnection() {
        String response = ttcDataService.fetchAndBroadcastTtcData();
        return ResponseEntity.ok(response);
    }
}
