dependencies {
    compile("org.mongodb:mongodb-driver-sync:4.0.4")
    compile(project(":rhea-core"))
}

tasks.jar {
    archiveBaseName.set("rhea-mongo")
}