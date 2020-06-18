dependencies {
    compile(project(":rhea-core"))

    implementation("com.bettercloud:vault-java-driver:5.1.0")
}

tasks.jar {
    archiveBaseName.set("rhea-vault")
}