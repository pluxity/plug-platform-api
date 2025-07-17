package com.pluxity.facility.path;

import com.pluxity.facility.Facility;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class FacilityPath extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Facility facility;

    private String name;

    @Enumerated(EnumType.STRING)
    private PathType pathType;

    @Column(columnDefinition = "text")
    private String path;

    @Builder
    public FacilityPath(Facility facility, String name, PathType pathType, String path) {
        this.facility = facility;
        this.name = name;
        this.pathType = pathType;
        this.path = path;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePathType(PathType pathType) {
        this.pathType = pathType;
    }

    public void updatePath(String path) {
        this.path = path;
    }
}
