import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    val kotlinVersion = "2.2.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.springframework.boot") version "3.5.4" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    kotlin("plugin.jpa") version kotlinVersion
    id("com.diffplug.spotless") version "7.2.1"
}

allprojects {
    group = "com.pluxity"
    version = "1.0.0"
    repositories { mavenCentral() }
}

subprojects {

    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension>("kotlin") {
            compilerOptions { freeCompilerArgs.addAll("-Xjsr305=strict") }
        }
    }

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.boot:spring-boot-starter")

        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("io.micrometer:micrometer-registry-prometheus")

        runtimeOnly("com.h2database:h2")
        runtimeOnly("org.postgresql:postgresql")

        implementation("org.flywaydb:flyway-core")
        runtimeOnly("org.flywaydb:flyway-database-postgresql")

        implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.11.0")

        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
        implementation("org.springdoc:springdoc-openapi-starter-common:2.8.9")
        implementation("io.github.oshai:kotlin-logging:7.0.12")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("com.linecorp.kotlin-jdsl:jpql-dsl:3.5.5")
        implementation("com.linecorp.kotlin-jdsl:jpql-render:3.5.5")
        implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:3.5.5")

        testImplementation("org.mockito:mockito-core")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.junit.jupiter:junit-jupiter-api")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
        testImplementation("io.mockk:mockk:1.14.5")
        testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.getByName("annotationProcessor"))
        }
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<BootJar> {
        enabled = false
    }

    spotless {
        kotlin {
            target("src/**/*.kt")
            ktlint()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
