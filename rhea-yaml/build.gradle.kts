project.ext.set("artifactName", "rhea-yaml")

dependencies {
    compile(project(":rhea-core"))
    compile("org.yaml:snakeyaml:1.18")
}