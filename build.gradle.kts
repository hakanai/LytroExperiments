plugins {
    kotlin("jvm") version "2.1.0"
    kotlin("plugin.serialization") version "2.1.0"
}

group = "garden.ephemeral"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("com.squareup.okio:okio:3.9.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0-RC")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest:kotest-framework-api:5.9.1")
    testRuntimeOnly("io.kotest:kotest-runner-junit5:5.9.1")
}

tasks.test {
    useJUnitPlatform()
}
