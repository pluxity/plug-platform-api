package base.entity

import com.pluxity.global.entity.BaseEntity
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime

fun <T : BaseEntity> T.withAudit(
    createdAt: LocalDateTime = LocalDateTime.parse("2025-08-01T12:00:00"),
    updatedAt: LocalDateTime = createdAt,
    createdBy: String? = "tester",
    updatedBy: String? = "tester",
) = apply {
    ReflectionTestUtils.setField(this, "createdAt", createdAt)
    ReflectionTestUtils.setField(this, "updatedAt", updatedAt)
    ReflectionTestUtils.setField(this, "createdBy", createdBy)
    ReflectionTestUtils.setField(this, "updatedBy", updatedBy)
}
