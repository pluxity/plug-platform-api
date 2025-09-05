package com.pluxity.global.utils

import java.util.function.Function

object MappingUtils {
    fun <T> makeCategoryTree(
        list: List<T>,
        idGetter: Function<T, Long?>,
        parentIdGetter: Function<T, Long?>,
        childrenGetter: Function<T, MutableList<T>>,
    ): List<T> {
        val map =
            list
                .mapNotNull { item ->
                    idGetter.apply(item)?.let { id -> id to item }
                }.toMap()
        val roots = mutableListOf<T>()
        for (item in list) {
            val parentId = parentIdGetter.apply(item)
            if (parentId == null) {
                roots.add(item)
            } else {
                map[parentId]?.let { parent ->
                    childrenGetter.apply(parent).add(item)
                }
            }
        }
        return roots
    }
}
