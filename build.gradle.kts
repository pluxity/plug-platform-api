import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    val kotlinVersion = "1.9.25"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    id("org.springframework.boot") version "3.5.4" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    kotlin("plugin.jpa") version kotlinVersion
    id("com.diffplug.spotless") version "6.25.0"

    // kotlin 에서 lombok 사용이 가능해지게 만들어주는 플러그인
    kotlin("plugin.lombok") version kotlinVersion
    id("io.freefair.lombok") version "8.14"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

subprojects {
    group = "com.pluxity"
    version = "1.0.0"

    apply(plugin = "java")
    apply(plugin = "java-library")
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

    dependencies {
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.boot:spring-boot-starter")

        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("io.micrometer:micrometer-registry-prometheus")

        runtimeOnly("com.h2database:h2")
        runtimeOnly("org.postgresql:postgresql")

        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")

        implementation("org.flywaydb:flyway-core")

        implementation("com.github.gavlyukovskiy:p6spy-spring-boot-starter:1.11.0")

        implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9")
        implementation("org.springdoc:springdoc-openapi-starter-common:2.8.9")
        implementation("io.github.oshai:kotlin-logging:7.0.12")
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

        testImplementation("org.projectlombok:lombok")
        testImplementation("org.mockito:mockito-core")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.junit.jupiter:junit-jupiter-api")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
        testImplementation("io.mockk:mockk:1.14.5")
        testImplementation("io.kotest:kotest-runner-junit5:5.9.1")

        testAnnotationProcessor("org.projectlombok:lombok")
    }

    configurations {
        compileOnly {
            extendsFrom(configurations.getByName("annotationProcessor"))
        }
    }

    repositories {
        mavenCentral()
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<BootJar> {
        enabled = false
    }

    spotless {
        java {
            target("src/**/*.java")
            importOrder(
                "java",
                "jakarta",
                "javax",
                "com",
                "org",
                ""
            )
            removeUnusedImports()
            googleJavaFormat()
            indentWithTabs(2)
            indentWithSpaces(4)
            trimTrailingWhitespace()
            endWithNewline()
        }
        kotlin {
            target("src/**/*.kt")
            ktlint()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
