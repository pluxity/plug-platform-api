package com.pluxity.cctv.repository.impl

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.pluxity.cctv.entity.Cctv
import com.pluxity.cctv.repository.CctvCustomRepository
import com.pluxity.facility.Facility
import com.pluxity.feature.entity.Feature
import com.pluxity.global.annotation.CheckPermission
import com.pluxity.permission.ResourceType
import com.pluxity.user.entity.PermissionAction
import org.springframework.stereotype.Repository

@Repository
class CctvCustomRepositoryImpl(
    private val kotlinJdslJpqlExecutor: KotlinJdslJpqlExecutor,
) : CctvCustomRepository {
    @CheckPermission(resourceType = ResourceType.CCTV)
    override fun findByIdOrNullCustom(id: String): Cctv? =
        kotlinJdslJpqlExecutor
            .findAll(limit = 1) {
                select(entity(Cctv::class))
                    .from(entity(Cctv::class))
                    .where(path(Cctv::id).equal(id))
            }.firstOrNull()

    @CheckPermission(action = PermissionAction.READ_LIST, resourceType = ResourceType.CCTV)
    override fun findAllByFacilityIdIfPresent(facilityId: Long?): List<Cctv> =
        kotlinJdslJpqlExecutor
            .findAll {
                select(entity(Cctv::class))
                    .from(
                        entity(Cctv::class),
                        leftFetchJoin(Cctv::feature),
                        leftFetchJoin(Feature::facility),
                    ).where(
                        and(
                            facilityId?.let { path(Facility::id).eq(it) },
                        ),
                    )
            }.filterNotNull()
}
