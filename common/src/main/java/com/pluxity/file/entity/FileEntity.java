package com.pluxity.file.entity;

import com.pluxity.file.constant.FileStatus;
import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

    @Column(name = "content_type",nullable = false)
    private String contentType;

    @Column(name = "file_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private FileStatus fileStatus;

    @Builder
    public FileEntity(String filePath, String originalFileName, String contentType) {
        this.filePath = filePath;
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.fileStatus = FileStatus.TEMP;
    }

    public void makeComplete(String filePath) {
        this.filePath = filePath;
        this.fileStatus = FileStatus.COMPLETE;
    }
}
