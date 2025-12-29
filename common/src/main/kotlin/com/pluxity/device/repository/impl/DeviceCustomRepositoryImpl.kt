package com.pluxity.device.repository.impl

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.pluxity.device.entity.Device
import com.pluxity.device.repository.DeviceCustomRepository
import com.pluxity.facility.Facility
import com.pluxity.feature.entity.Feature
import com.pluxity.global.annotation.CheckPermission
import com.pluxity.user.entity.PermissionCheckType
import org.springframework.stereotype.Repository

@Repository
class DeviceCustomRepositoryImpl(
    private val kotlinJdslJpqlExecutor: KotlinJdslJpqlExecutor,
) : DeviceCustomRepository {
    @CheckPermission
    override fun findByIdOrNullCustom(id: String): Device? =
        kotlinJdslJpqlExecutor
            .findAll(limit = 1) {
                select(entity(Device::class))
                    .from(entity(Device::class))
                    .where(path(Device::id).equal(id))
            }.firstOrNull()

    @CheckPermission(phase = PermissionCheckType.ITEM_LIST)
    override fun findAllByFacilityIdIfPresent(facilityId: Long?): List<Device> =
        kotlinJdslJpqlExecutor
            .findAll {
                select(
                    entity(Device::class),
                ).from(
                    entity(Device::class),
                    leftFetchJoin(Device::category),
                    leftFetchJoin(Device::feature),
                    leftFetchJoin(Feature::facility),
                ).where(
                    and(
                        facilityId?.let { path(Facility::id).eq(it) },
                    ),
                )
            }.filterNotNull()
}
