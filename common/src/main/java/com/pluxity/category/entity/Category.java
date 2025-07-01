package com.pluxity.category.entity;

import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.entity.BaseEntity;
import com.pluxity.global.exception.CustomException;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "category")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    @Builder
    public Category(String name, CategoryType type, Category parent) {
        this.name = name;
        this.type = type;
        assignToParent(parent);
        validateDepth();
    }

    public boolean isRoot() {
        return parent == null;
    }

    public int getDepth() {
        return isRoot() ? 1 : parent.getDepth() + 1;
    }

    public void assignToParent(Category newParent) {
        validateParentType(newParent);
        
        if (this.parent != null) {
            this.parent.children.remove(this);
        }

        this.parent = newParent;

        if (newParent != null) {
            newParent.children.add(this);
        }

        validateDepth();
    }

    public void updateName(String name) {
        this.name = name;
    }

    private void validateParentType(Category newParent) {
        if (newParent != null && !this.type.equals(newParent.type)) {
            throw new CustomException(ErrorCode.INVALID_CATEGORY_TYPE);
        }
    }

    private void validateDepth() {
        if (getDepth() > type.getMaxDepth()) {
            throw new CustomException(ErrorCode.EXCEED_CATEGORY_DEPTH);
        }
    }
}
