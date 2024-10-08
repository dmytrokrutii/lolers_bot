plugins {
    id("application")
}

group = "org.lolers"
version = "1.0"

repositories {
    mavenCentral()
}

application {
    mainClass.set("org.lolers.Main")
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Assembles a fat JAR containing all dependencies."

    archiveClassifier.set("fat")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
    dependsOn("jar")
}

dependencies {
    implementation("org.telegram:telegrambots:6.9.7.1")
    implementation("io.javalin:javalin:6.3.0")
    implementation("com.google.inject:guice:7.0.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("net.engio:mbassador:1.3.2")
    implementation("com.konghq:unirest-java:3.14.5")
    implementation("it.sauronsoftware.cron4j:cron4j:2.2.5")
    implementation("com.assemblyai:assemblyai-java:2.3.1")
}

tasks.test {
    useJUnitPlatform()
}
