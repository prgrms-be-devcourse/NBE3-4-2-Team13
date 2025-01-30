package com.app.backend.domain.category.controller;


import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.backend.domain.category.dto.CategoryDto;
import com.app.backend.domain.category.dto.CategoryPageDto;
import com.app.backend.domain.category.dto.CategoryReqBody;
import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.category.service.CategoryService;
import com.app.backend.global.dto.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/categories")
public class CategoryController {

	private final CategoryService categoryService;

	@PostMapping
	public ApiResponse<CategoryDto> createCategory(
		@RequestBody CategoryReqBody request
	) {
		Category category = categoryService.create(request.name());

		CategoryDto categoryDto = CategoryDto.from(category);

		return ApiResponse.of(
			true,
			"201",
			"%s 카테고리가 생성되었습니다.".formatted(category.getName()),
			categoryDto
		);
	}

	@GetMapping
	public ApiResponse<CategoryPageDto> getCategories(
		@PageableDefault(page = 0, size = 10) Pageable pageable
	) {
		CategoryPageDto categoryPageDto = categoryService.getCategories(pageable);

		return ApiResponse.of(
			true,
			"200",
			"카테고리 목록 조회",
			categoryPageDto
		);
	}

	@PatchMapping("/{id}")
	public ApiResponse<CategoryDto> modifyCategory(
		@PathVariable Long id,
		@RequestBody CategoryReqBody modifyRequest
	) {

		Category category = categoryService.findById(id);

		categoryService.modify(category, modifyRequest.name());

		CategoryDto categoryDto = CategoryDto.from(category);

		return ApiResponse.of(
			true,
			"200",
			"%d번 카테고리가 수정되었습니다.".formatted(category.getId()),
			categoryDto
		);
	}

}
