package com.pluxity.domains.controller;

import com.pluxity.domains.dto.TestAResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestControllerA {

    @GetMapping("/testA/test")
    public TestAResponse test() {
        return TestAResponse.builder().name("testA").code("A").build();
    }


}
