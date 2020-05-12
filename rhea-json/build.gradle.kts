project.ext.set("artifactName", "rhea-json")

dependencies {
    implementation("com.beust:klaxon:5.0.1")

    compile(project(":rhea-core"))
}