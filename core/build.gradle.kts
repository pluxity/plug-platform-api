tasks.named("bootJar") { enabled = true }
tasks.jar { enabled = false }

dependencies {
    implementation(project(":common"))
    testImplementation(testFixtures(project(":common")))
}