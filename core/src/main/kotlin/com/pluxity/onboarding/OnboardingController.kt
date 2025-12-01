package com.pluxity.onboarding

import com.pluxity.global.annotation.ResponseCreated
import com.pluxity.onboarding.dto.AdminUserCreateRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/onboarding")
class OnboardingController(
    private val service: OnboardingService
) {
    @PostMapping("/users")
    @ResponseCreated(path = "/api/v1/onboarding/users/{id}")
    fun createAdminUser(
        @RequestBody @Valid request: AdminUserCreateRequest
    ): ResponseEntity<Long> = ResponseEntity.ok(service.createAdminUser(request))
}