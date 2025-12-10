package com.pluxity.onboarding.dto

data class OnboardingStationResponse(
    val name: String,
    val lines: List<OnboardingLineResponse>,
)
