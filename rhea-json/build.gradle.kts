dependencies {
    implementation("com.beust:klaxon:5.0.1")

    compile(project(":rhea-core"))
}

tasks.jar {
    archiveBaseName.set("rhea-json")
}