package com.pluxity.authentication.controller;

import com.pluxity.authentication.dto.SignInRequest;
import com.pluxity.authentication.dto.SignInResponse;
import com.pluxity.authentication.dto.SignUpRequest;
import com.pluxity.authentication.service.AuthenticationService;
import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.global.response.CreatedResponseBody;
import com.pluxity.global.response.DataResponseBody;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/sign-up")
    @ResponseCreated(path = "/users/me")
    public ResponseEntity<CreatedResponseBody> signUp(@RequestBody SignUpRequest dto) {
        return ResponseEntity.ok(CreatedResponseBody.of(authenticationService.signUp(dto)));
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
