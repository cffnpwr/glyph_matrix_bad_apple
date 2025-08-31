plugins {
    kotlin("jvm")
    application
}


dependencies {
    implementation("org.bytedeco:javacv-platform:1.5.9")
    implementation("com.github.ajalt.clikt:clikt:4.2.1")
    implementation("com.charleskorn.kaml:kaml:0.55.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

application {
    mainClass.set("dev.cffnpwr.frameGenerator.FrameGeneratorKt")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set("frame-generator")
    manifest {
        attributes["Main-Class"] = "dev.cffnpwr.frameGenerator.FrameGeneratorKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}