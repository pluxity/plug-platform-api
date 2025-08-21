tasks.named("bootJar") { enabled = true }
tasks.jar { enabled = true }

dependencies {
    implementation(project(":common"))
    implementation(project(":core"))
    testImplementation(testFixtures(project(":common")))
}