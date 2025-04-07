package com.pluxity.category.entity;

import com.pluxity.global.constant.ErrorCode;
import com.pluxity.global.entity.BaseEntity;
import com.pluxity.global.exception.CustomException;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@MappedSuperclass
@Getter
public abstract class Category<T extends Category<T>> extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    protected String name;

    @ManyToOne(fetch = FetchType.LAZY)
    protected T parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    protected List<T> children = new ArrayList<>();

    public abstract int getMaxDepth();


    public boolean isRoot() {
        return parent == null;
    }

    public int getDepth() {
        return isRoot() ? 1 : parent.getDepth() + 1;
    }

    public void assignToParent(T Parent) {
        this.parent = parent;
        this.validateDepth();
    }

    public void validateDepth() {
        if (getDepth() > getMaxDepth()) {
            throw new CustomException(ErrorCode.EXCEED_CATEGORY_DEPTH);
        }
    }
}
