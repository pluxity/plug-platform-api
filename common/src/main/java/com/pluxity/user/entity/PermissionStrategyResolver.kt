package com.pluxity.user.entity

import com.pluxity.global.annotation.ResolvePermission
import jakarta.annotation.PostConstruct
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.util.EnumMap

@Component
class PermissionStrategyResolver(
    private val applicationContext: ApplicationContext,
) {
    private val strategyMap: MutableMap<PermissionType, PermissionStrategy> =
        EnumMap(PermissionType::class.java)

    @PostConstruct
    fun initializeStrategies() {
        val beans = applicationContext.getBeansWithAnnotation(ResolvePermission::class.java)

        beans.values
            .filterIsInstance<PermissionStrategy>()
            .forEach { strategy ->
                val annotation = strategy.javaClass.getAnnotation(ResolvePermission::class.java)
                strategyMap[annotation.value] = strategy
            }
    }

    fun resolve(type: PermissionType): PermissionStrategy =
        strategyMap[type] ?: throw IllegalArgumentException("No strategy found for type $type")
}
