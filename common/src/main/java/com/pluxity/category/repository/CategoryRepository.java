package com.pluxity.category.repository;

import com.pluxity.category.entity.Category;
import com.pluxity.category.entity.CategoryType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByTypeAndParentIsNull(CategoryType type);
    
    List<Category> findByType(CategoryType type);
} 