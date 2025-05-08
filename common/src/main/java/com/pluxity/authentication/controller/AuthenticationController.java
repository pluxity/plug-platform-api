package com.pluxity.authentication.controller;

import com.pluxity.authentication.dto.SignInRequest;
import com.pluxity.authentication.dto.SignUpRequest;
import com.pluxity.authentication.service.AuthenticationService;
import com.pluxity.global.annotation.ResponseCreated;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @ResponseCreated(path = "/users/me")
    @PostMapping("/sign-up")
    public ResponseEntity<Long> signUp(@RequestBody SignUpRequest dto) {
        return ResponseEntity.ok(authenticationService.signUp(dto));
    }

    @PostMapping(value = "/sign-in", produces = "application/json")
    @ResponseCreated(path = "/users/me")
    public ResponseEntity<Void> signIn(
            @RequestBody SignInRequest signInRequestDto,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authenticationService.signIn(signInRequestDto, request, response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/sign-out", produces = "application/json")
    public ResponseEntity<Void> signOut(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authenticationService.signOut(request, response);

        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/refresh-token")
    public ResponseEntity<Void> refreshToken(HttpServletRequest request,
                                                        HttpServletResponse response) {
        authenticationService.refreshToken(request, response);
        return ResponseEntity.noContent().build();
    }
}
