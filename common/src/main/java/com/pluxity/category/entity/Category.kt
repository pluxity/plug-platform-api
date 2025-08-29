package com.pluxity.category.entity

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.entity.BaseEntity
import com.pluxity.global.exception.CustomException
import jakarta.persistence.*
import lombok.Getter
import lombok.Setter

@MappedSuperclass
@Getter
abstract class Category<T : Category<T?>?> : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    @JvmField
    @Column(nullable = false)
    @Setter
    protected var name: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    protected var parent: T? = null

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    protected var children: MutableList<T?> = ArrayList<T?>()

    open val maxDepth: Int
        get() = 2

    val isRoot: Boolean
        get() = parent == null

    val depth: Int
        get() = if (this.isRoot) 1 else parent.getDepth() + 1

    fun updateName(name: String?) {
        this.name = name
    }

    fun assignToParent(newParent: T?) {
        if (this.parent != null) {
            this.parent.getChildren().remove(this)
        }

        this.parent = newParent

        if (newParent != null) {
            newParent.getChildren().add(this as T)
        }

        this.validateDepth()
    }

    fun validateDepth() {
        if (this.depth > this.maxDepth) {
            throw CustomException(ErrorCode.EXCEED_CATEGORY_DEPTH)
        }
    }
}
