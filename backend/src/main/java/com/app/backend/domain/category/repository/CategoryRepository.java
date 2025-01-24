package com.app.backend.domain.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.backend.domain.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
