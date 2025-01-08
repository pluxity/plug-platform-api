package com.pluxity.authentication.controller;

import static com.pluxity.global.constant.SuccessCode.SUCCESS;
import static com.pluxity.global.constant.SuccessCode.SUCCESS_CREATE;

import com.pluxity.authentication.dto.SignInRequestDto;
import com.pluxity.authentication.dto.SignInResponseDto;
import com.pluxity.authentication.dto.SignUpRequestDto;
import com.pluxity.authentication.service.AuthenticationService;
import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ResponseBody;
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

    @GetMapping
    public ResponseEntity<ResponseBody> test() {
        return ResponseEntity.ok(ResponseBody.of(SUCCESS));
    }

    @PostMapping(value = "/sign-up")
    public ResponseBody signUp(@RequestBody SignUpRequestDto signUpRequestDto) {
        authenticationService.signUp(signUpRequestDto);
        return ResponseBody.of(SUCCESS_CREATE);
    }

    @PostMapping(value = "/sign-in", produces = "application/json")
    public DataResponseBody<SignInResponseDto> signIn(
            @RequestBody SignInRequestDto signInRequestDto) {
        return DataResponseBody.of(authenticationService.signIn(signInRequestDto));
    }

    @PostMapping(value = "/refresh-token")
    public DataResponseBody<SignInResponseDto> refreshToken(HttpServletRequest request) {
        return DataResponseBody.of(authenticationService.refreshToken(request));
    }
}
