package com.pluxity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestControllerB {
    @GetMapping("/testB/test")
    public TestBResponse test() {
        return TestBResponse.builder()
                .name("testB")
                .description("B")
                .build();
    }
}
