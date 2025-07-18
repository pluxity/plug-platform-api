package com.pluxity.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "resource_permission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResourcePermission {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(nullable = false)
    private String resourceName;

    @Column(nullable = false)
    private Long resourceId;

    @Builder
    public ResourcePermission(String resourceName, Long resourceId, Role role) {
        this.resourceName = resourceName;
        this.resourceId = resourceId;
        this.role = role;
    }

    public boolean matches(String resourceName, Long resourceId) {
        return this.resourceName.equalsIgnoreCase(resourceName) && this.resourceId.equals(resourceId);
    }
}
