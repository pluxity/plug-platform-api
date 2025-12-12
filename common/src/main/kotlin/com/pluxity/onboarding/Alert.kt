package com.pluxity.onboarding

import com.pluxity.device.entity.Device
import com.pluxity.facility.Facility
import com.pluxity.global.entity.BaseEntity
import com.pluxity.user.entity.User
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

/**
 * 온보딩용 알람 저장 테이블
 *
 * OnboardingCollector에서 수집한 데이터중 미세먼지 농도가 나쁨 상태인 데이터를 해당 station을 관리하는 담당자에게 알림을 발생
 */

@Entity
class Alert(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne
    @JoinColumn(name = "user_id")
    var user: User? = null,
    @ManyToOne
    @JoinColumn(name = "device_id")
    var device: Device? = null,
    @ManyToOne
    @JoinColumn(name = "station_id")
    var facility: Facility? = null,
    val message: String? = null,
) : BaseEntity() {
    fun changeUser(user: User?) {
        this.user = user
    }

    fun changeDevice(device: Device?) {
        this.device = device
    }

    fun changeStation(facility: Facility) {
        this.facility = facility
    }
}
