import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
}

group = "uk.dsx.reactiveconfig"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val spekVersion = "2.0.8"

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0")
    implementation(kotlin("stdlib-jdk8"))

    testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
    testImplementation(kotlin("test"))
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
    testRuntimeOnly(kotlin("reflect"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}