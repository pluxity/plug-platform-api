package com.pluxity.facility.building;

import com.pluxity.facility.facility.Facility;
import jakarta.persistence.CascadeType; // Added
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType; // Added
import jakarta.persistence.GeneratedValue; // Added
import jakarta.persistence.GenerationType; // Added
import jakarta.persistence.Id; // Added
import jakarta.persistence.JoinColumn; // Added
import jakarta.persistence.OneToOne; // Added
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Entity
@Table(name = "building")
// @DiscriminatorValue("BUILDING") // Removed
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ConditionalOnProperty(name = "facility.building.enabled", havingValue = "true")
public class Building { // Removed "extends Facility"

    @Id // Added
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Added
    private Long id; // Added

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true) // Added
    @JoinColumn(name = "facility_id", referencedColumnName = "id", nullable = false) // Added
    private Facility facility; // Added

    @Builder
    public Building(String name, String description) {
        // super(name, description); // Removed
        this.facility = new Facility(name, null, description, "Initial comment for Building's facility"); // Added initialization
    }
}
