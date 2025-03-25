package com.pluxity.category.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategoryTest {

    @Test
    void 카테고리_생성_테스트() {
        // given
        String name = "테스트 카테고리";
        String description = "테스트 설명";

        // when
        Category category = Category.builder()
                .name(name)
                .description(description)
                .build();

        // then
        assertThat(category.getName()).isEqualTo(name);
        assertThat(category.getDescription()).isEqualTo(description);
        assertThat(category.getLevel()).isEqualTo(0);
        assertThat(category.getPath()).isEmpty();
        assertThat(category.isRoot()).isTrue();
        assertThat(category.isLeaf()).isTrue();
    }

    @Test
    void 자식_카테고리_추가_테스트() {
        // given
        Category parent = Category.builder()
                .name("부모 카테고리")
                .build();
        Category child = Category.builder()
                .name("자식 카테고리")
                .build();

        // when
        parent.addChild(child);

        // then
        assertThat(parent.getChildren()).hasSize(1);
        assertThat(parent.getChildren()).contains(child);
        assertThat(child.getParent()).isEqualTo(parent);
        assertThat(child.getLevel()).isEqualTo(1);
        assertThat(child.getPath()).isEqualTo(parent.getId() + "/" + child.getId());
    }

    @Test
    void 자식_카테고리_제거_테스트() {
        // given
        Category parent = Category.builder()
                .name("부모 카테고리")
                .build();
        Category child = Category.builder()
                .name("자식 카테고리")
                .build();
        parent.addChild(child);

        // when
        parent.removeChild(child);

        // then
        assertThat(parent.getChildren()).isEmpty();
        assertThat(child.getParent()).isNull();
        assertThat(child.getLevel()).isEqualTo(0);
        assertThat(child.getPath()).isEqualTo(String.valueOf(child.getId()));
    }

    @Test
    void 카테고리_이름_수정_테스트() {
        // given
        Category category = Category.builder()
                .name("기존 이름")
                .build();

        // when
        category.updateName("새 이름");

        // then
        assertThat(category.getName()).isEqualTo("새 이름");
    }

    @Test
    void 카테고리_이름_수정_시_null_체크() {
        // given
        Category category = Category.builder()
                .name("기존 이름")
                .build();

        // when & then
        assertThatThrownBy(() -> category.updateName(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Name must not be null");
    }

    @Test
    void 카테고리_설명_수정_테스트() {
        // given
        Category category = Category.builder()
                .name("테스트 카테고리")
                .description("기존 설명")
                .build();

        // when
        category.updateDescription("새 설명");

        // then
        assertThat(category.getDescription()).isEqualTo("새 설명");
    }
} 