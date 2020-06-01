dependencies {
    compile("mysql:mysql-connector-java:8.0.20")

    compile(project(":rhea-core"))
}

tasks.jar {
    archiveBaseName.set("rhea-jdbc")
}