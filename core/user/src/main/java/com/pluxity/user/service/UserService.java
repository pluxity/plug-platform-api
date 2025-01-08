package com.pluxity.user.service;

import static com.pluxity.global.constant.ErrorCode.NOT_FOUND_USER;

import com.pluxity.global.exception.CustomException;
import com.pluxity.user.dto.PatchDto;
import com.pluxity.user.dto.ResponseUserDto;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.UserRepository;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    @Transactional(readOnly = true)
    public List<ResponseUserDto> findAll() {
        List<User> users = repository.findAll();

        return users.stream()
                .map(
                        user ->
                                new ResponseUserDto(
                                        user.getUsername(), user.getName(), user.getCode(), user.getRole()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ResponseUserDto findById(Long id) {
        User user = repository.findById(id).orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        return ResponseUserDto.builder()
                .username(user.getUsername())
                .name(user.getName())
                .code(user.getCode())
                .role(user.getRole())
                .build();
    }

    @Transactional(readOnly = true)
    public ResponseUserDto findByUsername(String username) {
        User user =
                repository.findByUsername(username).orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        return ResponseUserDto.builder()
                .username(user.getUsername())
                .name(user.getName())
                .code(user.getCode())
                .role(user.getRole())
                .build();
    }

    @Transactional
    public void update(Long id, PatchDto dto) {
        User user = repository.findById(id).orElseThrow(() -> new CustomException(NOT_FOUND_USER));
        user.updateInfo(dto.name(), dto.code());
    }

    @Transactional
    public void update(String username, PatchDto dto) {
        User user =
                repository.findByUsername(username).orElseThrow(() -> new CustomException(NOT_FOUND_USER));
        user.updateInfo(dto.name(), dto.code());
    }

    @Transactional
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ResponseUserDto myInfo(Principal principal) {
        var username = principal.getName();
        User user =
                repository.findByUsername(username).orElseThrow(() -> new CustomException(NOT_FOUND_USER));

        return new ResponseUserDto(user.getUsername(), user.getName(), user.getCode(), user.getRole());
    }
}
