package com.pluxity.global.utils

import org.springframework.data.domain.Sort

object SortUtils {
    val orderByCreatedAtDesc = Sort.by(Sort.Direction.DESC, "createdAt")
}
