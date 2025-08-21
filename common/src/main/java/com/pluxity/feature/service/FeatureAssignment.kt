package com.pluxity.feature.service

import com.pluxity.feature.entity.Feature

interface FeatureAssignment {
    fun getType(): FeatureAssignType

    fun isAlreadyAssignedFeature(id: String): Boolean // 연결 대상에 이미 연결된 피처가 있는지 체크

    fun checkExistsByFeature(feature: Feature): Boolean // 피처가 이미 연결되어있는지 체크

    fun assignFeature(
        id: String,
        feature: Feature,
    )

    fun checkRevokeValidate(
        id: String,
        featureId: String,
    )

    fun revokeFeature(id: String)
}
