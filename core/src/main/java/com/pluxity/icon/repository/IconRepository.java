package com.pluxity.icon.repository;

import com.pluxity.icon.entity.Icon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IconRepository extends JpaRepository<Icon, Long> {
}
