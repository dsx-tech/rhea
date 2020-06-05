import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("jvm") version "1.3.41" apply false
    id("org.jetbrains.dokka") version "0.10.1"
    `maven-publish`
    signing
    java
}

buildscript {
    extra.apply {
        set("artifactGroup", "uk.dsxt")
        set("artifactVersion", "0.0.1-SNAPSHOT")
        set("spekVersion", "2.0.8")
    }

    repositories {
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.41")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:0.10.1")
    }
}


subprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "signing")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")

    repositories {
        mavenCentral()
        jcenter()
    }

    val spekVersion: String by rootProject.extra
    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0")
        implementation("org.slf4j:slf4j-simple:1.7.26")
        implementation("io.github.microutils:kotlin-logging:1.7.8")

        dokkaRuntime("org.jetbrains.dokka:dokka-fatjar:0.10.1")

        testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
        testImplementation(kotlin("test"))
        testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
        testRuntimeOnly(kotlin("reflect"))
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks {

        val sourcesJar by creating(Jar::class) {
            archiveClassifier.set("sources")
            from(sourceSets.main.get().allSource)
        }

        val dokka by getting(DokkaTask::class) {
            outputFormat = "javadoc"
            outputDirectory = "$buildDir/dokka"
            configuration {
                jdkVersion = 8
            }
        }

        artifacts {
            archives(sourcesJar)
            //archives("$buildDir/dokka")
            archives(jar)
        }
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                group = rootProject.ext["artifactGroup"] as String
                version = rootProject.ext["artifactVersion"] as String

                pom {
                    name.set("Rhea")
                    packaging = "jar"
                    description.set("Reactive configuration library for Kotlin and Java")
                    url.set("https://github.com/dsx-tech/rhea")

                    licenses {
                        license {
                            name.set("The MIT License")
                            url.set("http://www.opensource.org/licenses/mit-license.php")
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

        gradle.taskGraph.whenReady {
            if (allTasks.any { it is Sign }) {
                // Use Java's console to read from the console (no good for
                // a CI environment)
                val console = System.console()
                console.printf(
                    "\n\nWe have to sign some things in this build." +
                            "\n\nPlease enter your signing details.\n\n"
                )

                val id = console.readLine("PGP Key Id: ")
                val file = console.readLine("PGP Secret Key Ring File (absolute path): ")
                val password = console.readPassword("PGP Priva./te Key Password: ")

                allprojects {
                    extra["signing.keyId"] = id
                    extra["signing.secretKeyRingFile"] = file
                    extra["signing.password"] = password
                }

                console.printf("\nThanks.\n\n")
            }
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }
}
