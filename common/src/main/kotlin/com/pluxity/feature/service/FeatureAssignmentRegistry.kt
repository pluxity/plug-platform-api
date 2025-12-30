package com.pluxity.feature.service

import org.springframework.stereotype.Component

@Component
class FeatureAssignmentRegistry(
    assignments: List<FeatureAssignment> = emptyList(),
) {
    private val assignmentsByType: Map<FeatureAssignType, FeatureAssignment> =
        assignments.associateBy { it.type }

    fun get(type: FeatureAssignType): FeatureAssignment? = assignmentsByType[type]
}
