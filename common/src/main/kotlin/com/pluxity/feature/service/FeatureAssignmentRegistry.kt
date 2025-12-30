package com.pluxity.feature.service

import com.pluxity.feature.entity.Feature
import org.springframework.stereotype.Component

@Component
class FeatureAssignmentRegistry(
    assignments: List<FeatureAssignment> = emptyList(),
) {
    private val assignmentsByType: Map<FeatureAssignType, FeatureAssignment> =
        assignments.associateBy { it.type }

    fun get(type: FeatureAssignType): FeatureAssignment? = assignmentsByType[type]

    fun forEach(action: (FeatureAssignment) -> Unit) {
        assignmentsByType.values.forEach(action)
    }

    fun anyExistsByFeature(feature: Feature): Boolean = assignmentsByType.values.any { it.existsByFeature(feature) }
}
