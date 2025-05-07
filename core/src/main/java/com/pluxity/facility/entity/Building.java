package com.pluxity.facility.entity;

import com.pluxity.facility.domain.FacilityType;
import com.pluxity.file.entity.FileEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "building")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @Column(name = "address")
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @OneToMany(mappedBy = "facility", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Floor> floors = new ArrayList<>();

    @Builder
    public Building(String name, String description, String address, Double latitude, Double longitude, FacilityType facilityType) {
        this.facility = new Facility(name, description, facilityType != null ? facilityType : FacilityType.BUILDING);
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Facility 메서드 위임
    public String getName() {
        return facility.getName();
    }
    
    public String getDescription() {
        return facility.getDescription();
    }
    
    public FacilityType getFacilityType() {
        return facility.getFacilityType();
    }
    
    public FacilityCategory getCategory() {
        return facility.getCategory();
    }
    
    public FileEntity getDrawingFile() {
        return facility.getDrawingFile();
    }
    
    public FileEntity getThumbnailFile() {
        return facility.getThumbnailFile();
    }

    // 빌딩 속성 업데이트 메서드
    public void updateAddress(String address) {
        this.address = address;
    }

    public void updateLocation(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    // Facility 업데이트 위임 메서드
    public void updateName(String name) {
        facility.updateName(name);
    }
    
    public void updateDescription(String description) {
        facility.updateDescription(description);
    }
    
    public void updateDrawingFile(FileEntity drawingFile) {
        facility.updateDrawingFile(drawingFile);
    }
    
    public void updateThumbnailFile(FileEntity thumbnailFile) {
        facility.updateThumbnailFile(thumbnailFile);
    }
    
    public void assignCategory(FacilityCategory category) {
        facility.assignCategory(category);
    }
    
}
