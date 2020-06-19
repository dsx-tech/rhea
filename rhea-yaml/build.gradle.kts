dependencies {
    compile(project(":rhea-core"))
    compile("org.yaml:snakeyaml:1.18")
}

tasks.jar {
    archiveBaseName.set("rhea-yaml")
}