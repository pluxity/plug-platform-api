package com.pluxity.user.entity

import org.junit.jupiter.api.DisplayName
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class OnboardingUserTest {

    @Test
    @DisplayName("비밀번호 변경 시 password 필드가 변경 검증")
    fun changePassword_Success() {
        // given: 테스트할 User 준비
        val user = User(
            username = "testuser",
            password = "oldPassword",
            name = "테스트유저",
            code = null
        )

        // when: 비밀번호 변경 실행
        user.changePassword("newPassword")

        // then: password가 변경되었는지 검증
        assertThat(user.password).isEqualTo("newPassword")
    }

    @Test
    @DisplayName("비밀번호 변경 시 lastPasswordChangeDate가 현재 시간으로 갱신")
    fun changePassword_UpdatesLastChangeDate() {
        // given
        val user = User(
            username = "testuser",
            password = "oldPassword",
            name = "테스트유저",
            code = null
        )
        val beforeChangeDate = user.lastPasswordChangeDate

        // when
        Thread.sleep(1000)
        user.changePassword("newPassword")

        // then
        assertThat(user.lastPasswordChangeDate).isAfter(beforeChangeDate)
    }
}