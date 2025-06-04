package com.pluxity.domains.ttc;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TtcService {

    private final String currentHexDataStream =
            "0200143FB2001906020E020A7C015F5F1167116901011417D003"
                    + "0200133FB1001906020E020A6C028686216021620101310903"
                    + "0200133EB3001906020E020977028686215421560101227303";

    public String getParsedTtcDataAsJson() {
        log.debug("Fetching and parsing TTC data...");
        try {
            List<TtcParser.ParsedMessage> parsedMessages = TtcParser.parse(currentHexDataStream);

            String jsonData =
                    parsedMessages.stream()
                            .map(TtcParser.ParsedMessage::toJson)
                            .collect(Collectors.joining(",", "[", "]"));
            log.debug("Successfully parsed TTC data into JSON.");
            return jsonData;
        } catch (Exception e) {
            log.error("Error parsing TTC data: {}", e.getMessage(), e);
            return String.format(
                    "{\"error\": \"Failed to process TTC data: %s\"}", e.getMessage().replace("\"", "\\\""));
        }
    }
}
