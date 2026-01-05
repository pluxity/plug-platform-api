plugins {
    id("java-test-fixtures")
}

dependencies {
    implementation("software.amazon.awssdk:s3:2.30.24")

    api("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.6")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.6")
    implementation("org.zalando:logbook-spring-boot-starter:3.12.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:6.0.0")

    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.h2database:h2")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
    testFixturesImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testFixturesImplementation("io.mockk:mockk:1.13.5")
    testFixturesImplementation("com.ninja-squad:springmockk:4.0.2")
    testFixturesImplementation("org.springframework.security:spring-security-test")
    testFixturesImplementation(kotlin("stdlib"))

}