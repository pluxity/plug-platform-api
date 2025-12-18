package com.pluxity.category.entity

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.entity.BaseEntity
import com.pluxity.global.exception.CustomException
import jakarta.persistence.Column
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.OneToMany

@MappedSuperclass
abstract class Category<T : Category<T>>(
    @Column(nullable = false)
    var name: String = "",
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    open var parent: T? = null

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    open var children: MutableList<T> = mutableListOf()

    open val maxDepth: Int
        get() = 2

    val depth: Int
        get() = 1 + (parent?.depth ?: 0)

    fun requiredId(): Long = checkNotNull(this.id) { "Category ID is required" }

    fun isRoot(): Boolean = parent == null

    fun updateName(name: String) {
        this.name = name
    }

    @Suppress("UNCHECKED_CAST")
    fun assignToParent(newParent: T?) {
        val currentParent = this.parent
        currentParent?.children?.remove(this)

        this.parent = newParent

        newParent?.children?.add(this as T)

        validateDepth()
    }

    fun validateDepth() {
        if (depth > maxDepth) {
            throw CustomException(ErrorCode.EXCEED_CATEGORY_DEPTH)
        }
    }
}
