package com.pluxity.category.repository;

import com.pluxity.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository<T extends Category<T>> extends JpaRepository<T, Long> {
    Optional<T> findByName(String name);
    List<T> findByParentId(Long parentId);
    
    @Query("SELECT c FROM #{#entityName} c WHERE c.parent IS NULL")
    List<T> findRootCategories();
    
    @Query("SELECT c FROM #{#entityName} c WHERE c.path LIKE %:path%")
    List<T> findByPathContaining(@Param("path") String path);
} 