tasks.named("bootJar") { enabled = true }
tasks.jar { enabled = false }

dependencies {
    implementation(project(":common"))
    implementation(project(":core"))
    implementation(project(":collect"))
    testImplementation(testFixtures(project(":common")))
}