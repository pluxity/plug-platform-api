package com.pluxity.file.repository

import com.pluxity.file.entity.FileEntity
import org.springframework.data.jpa.repository.JpaRepository

interface FileRepository : JpaRepository<FileEntity, Long> {
    fun findByIdIn(ids: List<Long>): List<FileEntity>
}
