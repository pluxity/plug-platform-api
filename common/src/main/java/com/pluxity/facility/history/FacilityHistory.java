package com.pluxity.facility.history;

import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class FacilityHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long fileId;
    private Long facilityId;
    private String comment;

    @Builder
    public FacilityHistory(Long fileId, Long facilityId, String comment) {
        this.fileId = fileId;
        this.facilityId = facilityId;
        this.comment = comment;
    }
}
