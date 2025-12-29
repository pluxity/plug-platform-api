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
    implementation(project(":collect"))
    testImplementation(testFixtures(project(":common")))
}
