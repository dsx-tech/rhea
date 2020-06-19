dependencies {
    testCompile("com.h2database:h2:1.0.60")
    compile(project(":rhea-core"))
}

tasks.jar {
    archiveBaseName.set("rhea-jdbc")
}