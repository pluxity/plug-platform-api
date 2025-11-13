tasks.named("bootJar") { enabled = true }
tasks.jar { enabled = true }

dependencies {
    implementation(project(":common"))
    implementation(project(":core"))
    implementation(project(":collect"))
    testImplementation(testFixtures(project(":common")))
}