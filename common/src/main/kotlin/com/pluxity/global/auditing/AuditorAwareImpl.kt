package com.pluxity.global.auditing

import org.springframework.data.domain.AuditorAware
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.util.Optional

class AuditorAwareImpl : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> {
        val authentication = SecurityContextHolder.getContext().authentication

        val auditor =
            if (authentication == null ||
                !authentication.isAuthenticated ||
                authentication is AnonymousAuthenticationToken
            ) {
                "System"
            } else {
                authentication.name
            }

        return Optional.ofNullable(auditor)
    }
}
