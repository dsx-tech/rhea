import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    kotlin("jvm") version "1.3.41" apply false
    id("org.jetbrains.dokka") version "0.10.1" apply false
    `maven-publish`
    signing
    java
}

buildscript {
    extra.apply {
        set("artifactGroup", "uk.dsxt")
        set("artifactVersion", "0.0.1-SNAPSHOT")
        set("spekVersion", "2.0.8")
        set("junitVersion", "5.6.2")
    }

    repositories {
        mavenCentral()
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
    val junitVersion: String by rootProject.extra

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.0")
        implementation("org.slf4j:slf4j-simple:1.7.26")
        implementation("io.github.microutils:kotlin-logging:1.7.8")

        testImplementation("org.spekframework.spek2:spek-dsl-jvm:$spekVersion")
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        testImplementation(kotlin("test"))
        testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:$spekVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
        testRuntimeOnly(kotlin("reflect"))
    }

    tasks {
        test {
            useJUnitPlatform()
        }

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

        val javadocJar by creating(Jar::class) {
            dependsOn(dokka)
            archiveClassifier.set("javadoc")
            from("$buildDir/dokka")
        }
    }

    group = rootProject.ext["artifactGroup"] as String
    version = rootProject.ext["artifactVersion"] as String

    publishing {
        publications {
            create<MavenPublication>("rheaPublication") {
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])
                artifact(tasks["jar"])

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
                            id.set("dmitryv")
                            name.set("Dmitry Vologin")
                            email.set("dmit.vologin@gmail.com")
                        }
                        developer {
                            id.set("alexandrao")
                            name.set("Alexandra Osipova")
                            email.set("alexosipova13@gmail.com")
                        }
                        developer {
                            id.set("antonp")
                            name.set("Anton Plotnikov")
                            email.set("plotnikovanton@gmail.com")
                        }
                        developer {
                            id.set("philippd")
                            name.set("Philipp Dolgolev")
                            email.set("phil.dolgolev@gmail.com")
                        }
                    }

                    scm {
                        connection.set("scm:git:git:dsx-tech/rhea.git")
                        url.set("https://github.com/dsx-tech/rhea")
                    }
                }
            }
        }

        repositories {
            maven {
                val ossrhUsername: String by rootProject
                val ossrhPassword: String by rootProject

                val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
                val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"

                url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
                credentials {
                    username = ossrhUsername
                    password = ossrhPassword
                }
            }
        }
    }

    signing {
        sign(publishing.publications["rheaPublication"])
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.named<Wrapper>("wrapper") {
    gradleVersion = "5.2.1"
    distributionUrl = "https://services.gradle.org/distributions/gradle-5.2.1-bin.zip"
}