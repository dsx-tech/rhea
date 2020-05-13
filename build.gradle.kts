import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.41"
    `maven-publish`
    signing
}

project.ext.set("artifactGroup", "uk.dsxt")
project.ext.set("artifactVersion", "1.0")

buildscript {

    dependencies {
        classpath(kotlin("gradle-plugin", "1.3.41"))
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "signing")
    apply(plugin = "maven-publish")

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

    val sourceJar by tasks.creating(Jar::class) {
        archiveClassifier.set("sources")
        from(project.sourceSets.main.get())
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                group = rootProject.ext["artifactGroup"].toString()
                version = rootProject.ext["artifactVersion"].toString()
                artifact(sourceJar)

                pom {
                    name.set("Rhea")
                    packaging = "jar"
                    description.set("Reactive configuration library for Kotlin and Java")
                    url.set("https://github.com/dsx-tech/rhea")

                    licenses {
                        license {
                            name.set("The MIT License")
                            url.set("https://github.com/dsx-tech/rhea/blob/master/LICENSE")
                            distribution.set("repo")
                        }
                    }

                    developers {
                        developer {
                            id.set("johnd")
                            name.set("John Doe")
                            email.set("john.doe@example.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git:dsx-tech/rhea.git")
                        developerConnection.set("scm:git:ssh:dsx-tech/rhea.git")
                        url.set("https://github.com/dsx-tech/rhea")
                    }
                }
            }
        }

        repositories {
            maven {
                name = "myRepo"
                url = uri("file://${buildDir}/repo")
            }
        }

        signing {
            sign(publishing.publications["mavenJava"])
        }
    }
}