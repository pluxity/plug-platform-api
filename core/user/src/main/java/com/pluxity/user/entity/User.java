package com.pluxity.user.entity;

import com.pluxity.user.constant.Role;
import jakarta.persistence.*;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_name", nullable = false, unique = true, length = 20)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 10)
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "role")
    private Role role;

    @Builder
    public User(
            final String username,
            final String password,
            final String name,
            final String code,
            final Role role) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.code = code;
        this.role = Objects.requireNonNullElse(role, Role.USER);
    }

    public void changePassword(final String newPassword) {
        this.password = newPassword;
    }

    public void updateInfo(final String name, final String code) {
        this.name = name;
        this.code = code;
    }
}
