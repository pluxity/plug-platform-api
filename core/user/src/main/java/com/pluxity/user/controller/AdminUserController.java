package com.pluxity.user.controller;

import static com.pluxity.global.constant.SuccessCode.SUCCESS_DELETE;
import static com.pluxity.global.constant.SuccessCode.SUCCESS_PATCH;

import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ResponseBody;
import com.pluxity.user.dto.PatchDto;
import com.pluxity.user.dto.ResponseUserDto;
import com.pluxity.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService service;

    @GetMapping
    public DataResponseBody<List<ResponseUserDto>> getUsers() {
        return DataResponseBody.of(service.findAll());
    }

    @GetMapping(value = "/{id}")
    public DataResponseBody<ResponseUserDto> getUser(@PathVariable("id") Long id) {
        return DataResponseBody.of(service.findById(id));
    }

    @PutMapping(value = "/{id}")
    public ResponseBody updateUser(@PathVariable("id") Long id, @RequestBody PatchDto dto) {
        service.update(id, dto);
        return ResponseBody.of(SUCCESS_PATCH);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseBody deleteUser(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseBody.of(SUCCESS_DELETE);
    }
}
