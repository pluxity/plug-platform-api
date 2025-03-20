package com.pluxity.permission.entity;

import com.pluxity.global.entity.BaseEntity;
import com.pluxity.user.entity.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Table(name = "permission")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "permission_type")
@NamedEntityGraph(
    name = "Permission.withRoleAndUser",
    attributeNodes = {
        @NamedAttributeNode(value = "role", subgraph = "role-subgraph")
    },
    subgraphs = {
        @NamedSubgraph(
            name = "role-subgraph",
            attributeNodes = {
                @NamedAttributeNode(value = "userRoles", subgraph = "userRoles-subgraph")
            }
        ),
        @NamedSubgraph(
            name = "userRoles-subgraph",
            attributeNodes = {
                @NamedAttributeNode("user")
            }
        )
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "resource_id")
    private Long resourceId;

    protected Permission(Role role, Long resourceId) {
        this.role = Objects.requireNonNull(role, "Role must not be null");
        this.resourceId = resourceId;
    }

    public void changeRole(Role role) {
        this.role = Objects.requireNonNull(role, "Role must not be null");
    }

    public void changeResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public abstract boolean hasPermission(Long resourceId);
} 