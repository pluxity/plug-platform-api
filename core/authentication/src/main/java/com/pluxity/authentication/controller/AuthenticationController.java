package com.pluxity.authentication.controller;

import com.pluxity.authentication.dto.SignInRequest;
import com.pluxity.authentication.dto.SignInResponse;
import com.pluxity.authentication.service.AuthenticationService;
import com.pluxity.global.response.DataResponseBody;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping(value = "/sign-in", produces = "application/json")
    public DataResponseBody<SignInResponse> signIn(
            @RequestBody SignInRequest signInRequestDto) {
        return DataResponseBody.of(authenticationService.signIn(signInRequestDto));
    }

    @PostMapping(value = "/refresh-token")
    public DataResponseBody<SignInResponse> refreshToken(HttpServletRequest request) {
        return DataResponseBody.of(authenticationService.refreshToken(request));
    }
}
