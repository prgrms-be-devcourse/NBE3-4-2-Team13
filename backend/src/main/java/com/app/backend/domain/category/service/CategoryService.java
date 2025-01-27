package com.app.backend.domain.category.service;

import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.category.exception.CategoryErrorCode;
import com.app.backend.domain.category.exception.CategoryException;
import com.app.backend.domain.category.repository.CategoryRepository;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

	private final CategoryRepository categoryRepository;

	@Transactional
	public Category create(String name) {

		validateCategoryName(name); // 입력값 검증

		if (categoryRepository.existsByName(name)) {
			throw new CategoryException(CategoryErrorCode.CATEGORY_NAME_DUPLICATE);
		}

		Category category = Category.builder()
			.name(name)
			.build();

		return categoryRepository.save(category);
	}

	// 검증 메서드
	private void validateCategoryName(String name) {
		if (name == null || name.isBlank()) {
			throw new CategoryException(CategoryErrorCode.CATEGORY_NAME_REQUIRED);
		}
		if (name.length() > 10) {
			throw new CategoryException(CategoryErrorCode.CATEGORY_NAME_TOO_LONG);
		}
		if (categoryRepository.existsByName(name)) {
			throw new CategoryException(CategoryErrorCode.CATEGORY_NAME_DUPLICATE);
		}
	}
}
