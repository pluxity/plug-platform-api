package com.pluxity.temperaturehumidity.repository

import com.pluxity.feature.entity.Feature
import com.pluxity.temperaturehumidity.entity.TemperatureHumidity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface TemperatureHumidityRepository :
    JpaRepository<TemperatureHumidity, String>,
    TemperatureHumidityCustomRepository {
    @Modifying
    @Query("UPDATE TemperatureHumidity t SET t.feature = NULL WHERE t.feature = :feature")
    fun revokeByFeature(feature: Feature)

    fun existsByFeature(feature: Feature): Boolean

    @Query("SELECT t.id FROM TemperatureHumidity t WHERE t.id IN :ids")
    fun findExistingIds(ids: List<String>): List<String>
}
