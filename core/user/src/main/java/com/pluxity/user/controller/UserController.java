package com.pluxity.user.controller;

import static com.pluxity.global.constant.SuccessCode.SUCCESS_PATCH;

import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ResponseBody;
import com.pluxity.user.dto.PatchDto;
import com.pluxity.user.dto.ResponseUserDto;
import com.pluxity.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping("/me")
    public DataResponseBody<ResponseUserDto> getUser(Authentication authentication) {
        var username = authentication.getName();
        return DataResponseBody.of(service.findByUsername(username));
    }

    @PutMapping("/me")
    public ResponseBody updateUser(Authentication authentication, @RequestBody PatchDto dto) {
        service.update(authentication.getName(), dto);
        return ResponseBody.of(SUCCESS_PATCH);
    }
}
