package com.pluxity.user.entity

import com.pluxity.global.annotation.ResolvePermission
import org.springframework.stereotype.Component

@Component
class PermissionStrategyResolver(
    strategies: List<PermissionStrategy>,
) {
    private val strategyMap: Map<PermissionType, PermissionStrategy> =
        strategies.associateBy { strategy ->
            strategy.javaClass.getAnnotation(ResolvePermission::class.java).value
        }

    fun resolve(type: PermissionType): PermissionStrategy =
        strategyMap[type] ?: throw IllegalArgumentException("No strategy found for type $type")
}
