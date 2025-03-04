package com.pluxity.building.entity;

import com.pluxity.file.entity.FileEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "building")
@SQLDelete(sql = "UPDATE building SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "file_id")
    private FileEntity file;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Builder
    public Building(String name) {
        this.name = name;
    }

    public void update(String name) {
        this.name = name;
    }

    public void updateFile(FileEntity file) {
        this.file = file;
    }

    public void delete() {
        this.isDeleted = true;
    }
}