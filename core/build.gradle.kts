tasks.named("bootJar") { enabled = true }
tasks.jar { enabled = true }

dependencies {
    implementation(project(":common"))
}