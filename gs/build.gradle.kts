tasks.named("bootJar") { enabled = true }
tasks.jar { enabled = true }

sourceSets {
    test {
        resources {
            srcDir(project(":core").file("src/test/resources"))
        }
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":core"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("com.cronutils:cron-utils:9.2.1")
    testImplementation(testFixtures(project(":common")))
}
