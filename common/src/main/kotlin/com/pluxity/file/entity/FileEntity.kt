package com.pluxity.file.entity

import com.pluxity.file.constant.FileStatus
import com.pluxity.global.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "files")
@EntityListeners(AuditingEntityListener::class)
class FileEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "file_path", nullable = false, unique = true)
    var filePath: String,
    @Column(name = "original_file_name", nullable = false)
    var originalFileName: String,
    @Column(name = "content_type", nullable = false)
    var contentType: String,
) : BaseEntity() {
    @Column(name = "file_status", nullable = false)
    @Enumerated(EnumType.STRING)
    var fileStatus: FileStatus = FileStatus.TEMP

    fun requiredId(): Long = checkNotNull(this.id) { "FileEntity ID is required" }

    fun makeComplete(filePath: String) {
        this.filePath = filePath
        this.fileStatus = FileStatus.COMPLETE
    }
}
