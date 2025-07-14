package com.pluxity.file.repository;

import com.pluxity.file.entity.FileEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, Long> {
    List<FileEntity> findByIdIn(List<Long> ids);
}
