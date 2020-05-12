import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
}

buildscript {
    group = "uk.dsxt"
    version = "1.0"

    dependencies {
        classpath(kotlin("gradle-plugin", "1.3.41"))
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")

    repositories {
        mavenCentral()
        jcenter()
    }

    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0")
        implementation(kotlin("stdlib-jdk8"))

        testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.8")
        testImplementation(kotlin("test"))
        testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.8")
        testRuntimeOnly(kotlin("reflect"))

        compile("org.slf4j:slf4j-simple:1.7.26")
        compile("io.github.microutils:kotlin-logging:1.7.8")

    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}