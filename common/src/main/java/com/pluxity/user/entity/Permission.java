package com.pluxity.user.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "permission")
    private final Set<RolePermission> rolePermissions = new HashSet<>();

    @Column(nullable = false)
    private String resourceName;

    @Column(nullable = false)
    private Long resourceId;

    @Builder
    public Permission(String resourceName, Long resourceId) {
        this.resourceName = resourceName;
        this.resourceId = resourceId;
    }

    public boolean matches(String resourceName, Long resourceId) {
        return this.resourceName.equalsIgnoreCase(resourceName) && this.resourceId.equals(resourceId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(resourceName, that.resourceName)
                && Objects.equals(resourceId, that.resourceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceName, resourceId);
    }
}
