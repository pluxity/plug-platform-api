tasks.named("bootJar") { enabled = true }
tasks.jar { enabled = true }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation(project(":common"))
    implementation(project(":core"))
}