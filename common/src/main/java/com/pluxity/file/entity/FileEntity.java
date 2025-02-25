package com.pluxity.file.entity;

import com.pluxity.file.constant.FileStatus;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class FileEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_path",nullable = false, unique = true)
    private String filePath;

    @Column(name = "original_file_name",nullable = false)
    private String originalFileName;

    @Column(name = "file_type",nullable = false)
    private String fileType;

    @Column(name = "file_status", nullable = false)
    private FileStatus fileStatus;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "modified_at", nullable = false)
    private LocalDateTime modifiedAt;

    @Builder
    public FileEntity(String filePath, String originalFileName, String fileType) {
        this.filePath = filePath;
        this.originalFileName = originalFileName;
        this.fileType = fileType;
        this.fileStatus = FileStatus.TEMP;
    }

    public void makeComplete(String filePath) {
        this.filePath = filePath;
        this.fileStatus = FileStatus.COMPLETE;
    }
}
