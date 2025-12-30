package com.pluxity.temperaturehumidity.service

import com.pluxity.feature.entity.Feature
import com.pluxity.feature.service.FeatureAssignType
import com.pluxity.feature.service.FeatureAssignment
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.temperaturehumidity.dto.TemperatureHumidityCreateRequest
import com.pluxity.temperaturehumidity.dto.TemperatureHumidityResponse
import com.pluxity.temperaturehumidity.dto.TemperatureHumidityUpdateRequest
import com.pluxity.temperaturehumidity.dto.toTemperatureHumidityResponse
import com.pluxity.temperaturehumidity.entity.TemperatureHumidity
import com.pluxity.temperaturehumidity.repository.TemperatureHumidityRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TemperatureHumidityService(
    private val temperatureHumidityRepository: TemperatureHumidityRepository,
) : FeatureAssignment {
    override val type: FeatureAssignType = FeatureAssignType.THERMO_HYGROMETER

    @Transactional
    fun save(request: TemperatureHumidityCreateRequest): String =
        temperatureHumidityRepository
            .save(
                TemperatureHumidity(
                    id = request.id,
                    name = request.name,
                ),
            ).id

    @Transactional(readOnly = true)
    fun findById(id: String): TemperatureHumidityResponse = getTemperatureHumidity(id).toTemperatureHumidityResponse()

    private fun getTemperatureHumidity(id: String): TemperatureHumidity =
        temperatureHumidityRepository.findByIdOrNullCustom(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_DEVICE, id)

    @Transactional(readOnly = true)
    fun findAll(facilityId: Long? = null): List<TemperatureHumidityResponse> {
        val devices = temperatureHumidityRepository.findAllByFacilityIdIfPresent(facilityId)
        return devices.map { it.toTemperatureHumidityResponse() }
    }

    @Transactional
    fun putUpdate(
        id: String,
        request: TemperatureHumidityUpdateRequest,
    ) {
        val device = getTemperatureHumidity(id)
        device.putUpdate(request.name)
    }

    @Transactional
    fun delete(id: String) {
        val device = getTemperatureHumidity(id)
        device.clearAllRelations()
        temperatureHumidityRepository.deleteById(device.id)
    }

    override fun isAssigned(id: String): Boolean = getTemperatureHumidity(id).feature != null

    override fun existsByFeature(feature: Feature): Boolean = temperatureHumidityRepository.existsByFeature(feature)

    override fun assignFeature(
        id: String,
        feature: Feature,
    ) = getTemperatureHumidity(id).changeFeature(feature)

    override fun validateRevoke(
        id: String,
        featureId: String,
    ) {
        val device = getTemperatureHumidity(id)
        val deviceFeature = device.feature ?: throw CustomException(ErrorCode.DEVICE_NOT_ASSIGNED, id)
        if (deviceFeature.id != featureId) {
            throw CustomException(ErrorCode.DEVICE_MISMATCH)
        }
    }

    override fun clearFeatureFromTarget(id: String) {
        temperatureHumidityRepository.findByIdOrNull(id).let { it?.changeFeature(null) }
    }

    override fun revokeByFeature(feature: Feature) {
        temperatureHumidityRepository.revokeByFeature(feature)
    }
}
