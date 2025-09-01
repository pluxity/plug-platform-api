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
abstract class Category<T : Category<T>> : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long? = null

    @Column(nullable = false)
    open var name: String = ""

    @ManyToOne(fetch = FetchType.LAZY)
    open var parent: T? = null

    @OneToMany(mappedBy = "parent", orphanRemoval = true)
    open var children: MutableList<T> = mutableListOf()

    open val maxDepth: Int
        get() = 2

    val depth: Int
        get() = 1 + (parent?.depth ?: 0)

    fun isRoot(): Boolean = parent == null

    fun updateName(name: String) {
        this.name = name
    }

    @Suppress("UNCHECKED_CAST")
    fun assignToParent(newParent: T?) {
        val currentParent = this.parent
        if (currentParent != null) {
            currentParent.children.remove(this as T)
        }

        this.parent = newParent

        if (newParent != null) {
            newParent.children.add(this as T)
        }

        validateDepth()
    }

    fun validateDepth() {
        if (depth > maxDepth) {
            throw CustomException(ErrorCode.EXCEED_CATEGORY_DEPTH)
        }
    }
}
