package com.app.backend.domain.category.dto;

import java.util.List;

public record CategoryPageDto(
	List<CategoryDto> categories,
	int currentPage,
	int totalPages,
	long totalItems,
	int pageSize
) {
}
