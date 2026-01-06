package com.pluxity.temperaturehumidity.repository.impl

import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
import com.pluxity.facility.Facility
import com.pluxity.feature.entity.Feature
import com.pluxity.global.annotation.CheckPermission
import com.pluxity.permission.ResourceType
import com.pluxity.temperaturehumidity.entity.TemperatureHumidity
import com.pluxity.temperaturehumidity.repository.TemperatureHumidityCustomRepository
import com.pluxity.user.entity.PermissionAction
import org.springframework.stereotype.Repository

@Repository
class TemperatureHumidityCustomRepositoryImpl(
    private val kotlinJdslJpqlExecutor: KotlinJdslJpqlExecutor,
) : TemperatureHumidityCustomRepository {
    @CheckPermission(resourceType = ResourceType.TEMPERATURE_HUMIDITY)
    override fun findByIdOrNullCustom(id: String): TemperatureHumidity? =
        kotlinJdslJpqlExecutor
            .findAll(limit = 1) {
                select(entity(TemperatureHumidity::class))
                    .from(entity(TemperatureHumidity::class))
                    .where(path(TemperatureHumidity::id).equal(id))
            }.firstOrNull()

    @CheckPermission(action = PermissionAction.READ_LIST, resourceType = ResourceType.TEMPERATURE_HUMIDITY)
    override fun findAllByFacilityIdIfPresent(facilityId: Long?): List<TemperatureHumidity> =
        kotlinJdslJpqlExecutor
            .findAll {
                select(
                    entity(TemperatureHumidity::class),
                ).from(
                    entity(TemperatureHumidity::class),
                    leftFetchJoin(TemperatureHumidity::feature),
                    leftFetchJoin(Feature::facility),
                ).where(
                    and(
                        facilityId?.let { path(Facility::id).eq(it) },
                    ),
                )
            }.filterNotNull()
}
