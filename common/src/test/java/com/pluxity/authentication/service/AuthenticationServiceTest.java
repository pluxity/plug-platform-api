package com.pluxity.authentication.service;

import com.pluxity.authentication.dto.SignUpRequest;
import com.pluxity.authentication.security.JwtProvider;
import com.pluxity.user.entity.User;
import com.pluxity.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입을 할 수 있다")
    void signUp() {
        // given
        SignUpRequest request = new SignUpRequest(
                "testUser",
                "password123",
                "홍길동",
                "USER001"
        );

        String encodedPassword = "encodedPassword123";
        User user = User.builder()
                .username(request.username())
                .password(encodedPassword)
                .name(request.name())
                .code(request.code())
                .build();

        given(passwordEncoder.encode(request.password())).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willReturn(user);

        // when
        Long userId = authenticationService.signUp(request);

        // then
        assertThat(userId).isEqualTo(user.getId());
        verify(passwordEncoder).encode(request.password());
        verify(userRepository).save(any(User.class));
    }
} 