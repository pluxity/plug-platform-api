package com.pluxity.category.controller;

import com.pluxity.category.dto.CategoryCreateRequest;
import com.pluxity.category.dto.CategoryResponseC;
import com.pluxity.category.dto.CategoryUpdateRequest;
import com.pluxity.category.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private CategoryResponseC categoryResponse;

    @BeforeEach
    void setUp() {
        categoryResponse = new CategoryResponseC(
                1L,
                "테스트 카테고리",
                "테스트 설명",
                null,
                1,
                "/1",
                List.of()
        );
    }

    @Test
    void 카테고리_생성_테스트() {
        // given
        CategoryCreateRequest request = new CategoryCreateRequest(
                "테스트 카테고리",
                "테스트 설명",
                null
        );
        given(categoryService.create(any(CategoryCreateRequest.class)))
                .willReturn(categoryResponse);

        // when
        ResponseEntity<CategoryResponseC> response = categoryController.create(request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().name()).isEqualTo(request.name());
        verify(categoryService).create(request);
    }

    @Test
    void 카테고리_조회_테스트() {
        // given
        given(categoryService.findById(1L)).willReturn(categoryResponse);

        // when
        ResponseEntity<CategoryResponseC> response = categoryController.findById(1L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo(1L);
        verify(categoryService).findById(1L);
    }

    @Test
    void 모든_카테고리_조회_테스트() {
        // given
        List<CategoryResponseC> categories = Arrays.asList(categoryResponse);
        given(categoryService.findAll()).willReturn(categories);

        // when
        ResponseEntity<List<CategoryResponseC>> response = categoryController.findAll();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        verify(categoryService).findAll();
    }

    @Test
    void 루트_카테고리_조회_테스트() {
        // given
        List<CategoryResponseC> rootCategories = Arrays.asList(categoryResponse);
        given(categoryService.findRootCategories()).willReturn(rootCategories);

        // when
        ResponseEntity<List<CategoryResponseC>> response = categoryController.findRootCategories();

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        verify(categoryService).findRootCategories();
    }

    @Test
    void 부모_카테고리의_자식_카테고리_조회_테스트() {
        // given
        List<CategoryResponseC> childCategories = Arrays.asList(categoryResponse);
        given(categoryService.findByParentId(1L)).willReturn(childCategories);

        // when
        ResponseEntity<List<CategoryResponseC>> response = categoryController.findByParentId(1L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        verify(categoryService).findByParentId(1L);
    }

    @Test
    void 카테고리_수정_테스트() {
        // given
        CategoryUpdateRequest request = new CategoryUpdateRequest(
                "수정된 카테고리",
                "수정된 설명"
        );
        given(categoryService.update(any(Long.class), any(CategoryUpdateRequest.class)))
                .willReturn(categoryResponse);

        // when
        ResponseEntity<CategoryResponseC> response = categoryController.update(1L, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(categoryService).update(1L, request);
    }

    @Test
    void 카테고리_삭제_테스트() {
        // when
        ResponseEntity<Void> response = categoryController.delete(1L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(categoryService).delete(1L);
    }

    @Test
    void 카테고리_이동_테스트() {
        // given
        given(categoryService.move(any(Long.class), any(Long.class)))
                .willReturn(categoryResponse);

        // when
        ResponseEntity<CategoryResponseC> response = categoryController.move(1L, 2L);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(categoryService).move(1L, 2L);
    }
} 