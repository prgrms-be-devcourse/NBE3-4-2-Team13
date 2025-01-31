package com.app.backend.domain.category.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.app.backend.domain.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
	boolean existsByName(String name);

	Category findByName(String name);

	Page<Category> findByDisabledFalse(Pageable pageable);
}
