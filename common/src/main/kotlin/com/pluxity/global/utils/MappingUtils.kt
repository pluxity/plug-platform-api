package com.pluxity.global.utils

object MappingUtils {
    fun <T> makeCategoryTree(
        list: List<T>,
        idGetter: (T) -> Long?,
        parentIdGetter: (T) -> Long?,
        childrenGetter: T.() -> MutableList<T>,
    ): List<T> {
        val map =
            list
                .mapNotNull { item ->
                    idGetter(item)?.let { id -> id to item }
                }.toMap()
        val roots = mutableListOf<T>()
        list.forEach { item ->
            when (val parentId = parentIdGetter(item)) {
                null -> roots += item
                else -> map[parentId]?.childrenGetter()?.add(item)
            }
        }
        return roots
    }
}
