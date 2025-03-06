package com.pluxity.user.entity;

import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;

    @Builder
    public Role(String roleName) {
        this.roleName = Objects.requireNonNull(roleName, "Role name must not be null");
    }

    public void changeRoleName(String roleName) {
        this.roleName = Objects.requireNonNull(roleName, "Role name must not be blank");
    }
}
