plugins {
    id("java-test-fixtures")
}

tasks.named("bootJar") { enabled = true }
tasks.jar { enabled = true }

dependencies {
    implementation(project(":common"))
    testFixturesImplementation(kotlin("stdlib"))
    testImplementation(testFixtures(project(":common")))
}