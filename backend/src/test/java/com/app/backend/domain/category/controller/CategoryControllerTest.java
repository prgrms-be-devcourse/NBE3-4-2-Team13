package com.app.backend.domain.category.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.app.backend.domain.category.entity.Category;
import com.app.backend.domain.category.exception.CategoryErrorCode;
import com.app.backend.domain.category.exception.CategoryException;
import com.app.backend.domain.category.repository.CategoryRepository;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class CategoryControllerTest {

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private MockMvc mvc;

	@Test
	@DisplayName("카테고리 생성")
	void t1() throws Exception {
		String categoryName = "카테고리 new";

		String requestJson = """
                {
                    "name": "%s"
                }
                """.formatted(categoryName);

		mvc.perform(MockMvcRequestBuilders.post("/api/v1/admin/categories")
				.content(requestJson)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.code").value(HttpStatus.CREATED.value()))
			.andExpect(jsonPath("$.message").value("%s 카테고리가 생성되었습니다.".formatted(categoryName)))
			.andExpect(jsonPath("$.data.name").value(categoryName));

		Category category = categoryRepository.findByName(categoryName);
		assertNotNull(category);
		assertEquals(categoryName, category.getName());
	}


	@Test
	@DisplayName("카테고리 생성 - 공백 입력")
	void t2() throws Exception {
		String requestJson = """
                {
                    "name": ""
                }
                """;

		mvc.perform(MockMvcRequestBuilders.post("/api/v1/admin/categories")
				.content(requestJson)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.code").value("C001"))
			.andExpect(jsonPath("$.message").value("카테고리 이름은 필수입니다."));
	}

	@Test
	@DisplayName("카테고리 생성 - 10자 이상")
	void t3() throws Exception {
		String requestJson = """
			{
			    "name": "이름을10자이상입력하기"
			}
			""";

		mvc.perform(MockMvcRequestBuilders.post("/api/v1/admin/categories")
				.content(requestJson)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.code").value("C002"))
			.andExpect(jsonPath("$.message").value("카테고리 이름은 최대 10자까지 가능합니다."));
	}

	@Test
	@DisplayName("카테고리 생성 - 중복 이름")
	void t4() throws Exception {
		Category category = Category.builder()
			.name("카테고리1")
			.build();
		categoryRepository.save(category);

		String requestJson = """
			{
			    "name": "카테고리1"
			}
			""";

		mvc.perform(MockMvcRequestBuilders.post("/api/v1/admin/categories")
				.content(requestJson)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.code").value("C003"))
			.andExpect(jsonPath("$.message").value("이미 존재하는 카테고리입니다."));
	}

	@Test
	@DisplayName("카테고리 조회")
	void t5() throws Exception {
		for (int i = 1; i <= 11; i++) {
			Category category = Category.builder()
				.name("카테고리" + i)
				.build();
			categoryRepository.save(category);
		}

		ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/categories")
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("카테고리 목록 조회"))
			.andExpect(jsonPath("$.data.categories.length()").value(10))
			.andExpect(jsonPath("$.data.currentPage").value(1))
			.andExpect(jsonPath("$.data.totalPages").value(2))
			.andExpect(jsonPath("$.data.totalItems").value(11))
			.andExpect(jsonPath("$.data.pageSize").value(10));

		assertEquals(11, categoryRepository.count());
	}

	@Test
	@DisplayName("카테고리 조회 - 페이지2")
	void t6() throws Exception {
		for (int i = 1; i <= 11; i++) {
			Category category = Category.builder()
				.name("카테고리" + i)
				.build();
			categoryRepository.save(category);
		}

		ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/categories?page=1")
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("카테고리 목록 조회"))
			.andExpect(jsonPath("$.data.categories.length()").value(1))
			.andExpect(jsonPath("$.data.currentPage").value(2))
			.andExpect(jsonPath("$.data.totalPages").value(2))
			.andExpect(jsonPath("$.data.totalItems").value(11))
			.andExpect(jsonPath("$.data.pageSize").value(10));

		assertEquals(11, categoryRepository.count());
	}

	@Test
	@DisplayName("카테고리 수정")
	void t7() throws Exception {
		Category category = Category.builder()
			.name("수정전")
			.build();
		categoryRepository.save(category);

		String categoryName = "수정후";

		String requestJson = """
                {
                    "name": "%s"
                }
                """.formatted(categoryName);

		mvc.perform(MockMvcRequestBuilders.patch("/api/v1/admin/categories/{id}", category.getId())
				.content(requestJson)
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("%d번 카테고리가 수정되었습니다.".formatted(category.getId())))
			.andExpect(jsonPath("$.data.name").value(categoryName));

		Category updatedCategory = categoryRepository.findById(category.getId()).orElseThrow(() -> new CategoryException(
			CategoryErrorCode.CATEGORY_NOT_FOUND));
		assertEquals(categoryName, updatedCategory.getName()); // 반영되었는지 확인
	}

	@Test
	@DisplayName("카테고리 삭제")
	void t8() throws Exception {
		Category category = Category.builder()
			.name("카테고리")
			.build();
		categoryRepository.save(category);

		ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.delete("/api/v1/admin/categories/{id}", category.getId())
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("%d번 카테고리가 삭제되었습니다.".formatted(category.getId())));

		Category deletedCategory = categoryRepository.findById(category.getId())
			.orElseThrow(() -> new CategoryException(
				CategoryErrorCode.CATEGORY_NOT_FOUND));
		assertTrue(deletedCategory.getDisabled());
	}

	@Test
	@DisplayName("카테고리 삭제 후 카테고리 조회")
	void t9() throws Exception {
		for (int i = 1; i <= 11; i++) {
			Category category = Category.builder()
				.name("카테고리" + i)
				.build();
			categoryRepository.save(category);
		}

		Category category1 = categoryRepository.findAll().get(0);
		category1.softDelete();
		categoryRepository.save(category1);

		ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get("/api/v1/admin/categories")
				.contentType(MediaType.APPLICATION_JSON)
			)
			.andExpect(jsonPath("$.code").value(HttpStatus.OK.value()))
			.andExpect(jsonPath("$.message").value("카테고리 목록 조회"))
			.andExpect(jsonPath("$.data.categories.length()").value(10))
			.andExpect(jsonPath("$.data.currentPage").value(1))
			.andExpect(jsonPath("$.data.totalPages").value(1))
			.andExpect(jsonPath("$.data.totalItems").value(10))
			.andExpect(jsonPath("$.data.pageSize").value(10));

		assertEquals(11, categoryRepository.count());
	}
}
