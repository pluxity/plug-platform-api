package com.pluxity.domains.device.entity;

import com.pluxity.device.entity.DeviceCategory;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("NFLUX")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NfluxCategory extends DeviceCategory {

    @Column(name = "context_path")
    private String contextPath;

    // DTYPE 컬럼에 값을 설정하기 위한 필드
    @Column(name = "DTYPE", insertable = false, updatable = false)
    private String dtype = "DEVICE_BASE";

    @Builder(builderMethodName = "nfluxBuilder")
    public NfluxCategory(String name, DeviceCategory parent, String contextPath) {
        super(name, parent);
        this.contextPath = contextPath;
    }

    public void updateContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public int getMaxDepth() {
        return 1;
    }
}
