package com.app.backend.domain.category.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.backend.domain.category.dto.CategoryDto;
import com.app.backend.domain.category.dto.CategoryPageDto;
import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.category.exception.CategoryErrorCode;
import com.app.backend.domain.category.exception.CategoryException;
import com.app.backend.domain.category.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

	private final CategoryRepository categoryRepository;

	@Transactional
	public Category create(String name) {

		validateCategoryName(name); // 입력값 검증

		Category category = Category.builder()
			.name(name)
			.build();

		return categoryRepository.save(category);
	}

	// 카테고리 목록 페이지로 조회
	public CategoryPageDto getCategories(Pageable pageable) {
		Page<Category> categoryPage = categoryRepository.findByDisabledFalse(pageable);

		List<CategoryDto> categories = categoryPage
			.getContent()
			.stream()
			.map(CategoryDto::from)
			.toList();

		return new CategoryPageDto(
			categories,
			categoryPage.getNumber() + 1,
			categoryPage.getTotalPages(),
			categoryPage.getTotalElements(),
			categoryPage.getSize()
		);


	}

	// 검증 메서드
	private void validateCategoryName(String name) {
		if (name == null || name.isBlank()) {
			throw new CategoryException(CategoryErrorCode.CATEGORY_NAME_REQUIRED);
		}
		if (name.length() > 10) {
			throw new CategoryException(CategoryErrorCode.CATEGORY_NAME_TOO_LONG);
		}
		if (categoryRepository.existsByNameAndDisabled(name, false)) {
			throw new CategoryException(CategoryErrorCode.CATEGORY_NAME_DUPLICATE);
		}
	}

	public Category findById(Long id) {
		return categoryRepository.findById(id)
			.orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));
	}

	@Transactional
	public void modify(Category category, String newName) {

		validateCategoryName(newName); // 입력값 검증

		category.modifyName(newName);
	}

	@Transactional
	public void softDelete(Long id) {
		Category category = categoryRepository.findById(id)
			.orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));

		category.softDelete();

		categoryRepository.save(category);
	}
}
