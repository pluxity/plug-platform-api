package com.pluxity.feature.service

import org.springframework.stereotype.Component

@Component
class FeatureAssignmentRegistry(
    assignments: List<FeatureAssignment>,
) {
    private val map: Map<FeatureAssignType, FeatureAssignment> =
        assignments.associateBy { it.getType() }

    fun get(type: FeatureAssignType): FeatureAssignment = map[type] ?: throw IllegalArgumentException("Unsupported type: $type")

    fun all(): List<FeatureAssignment> = map.values.toList()
}
