plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

application {
   mainClass.set("org.lolers.Main")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("org.telegram:telegrambots:6.9.7.1")
    implementation("io.javalin:javalin:6.3.0")
    implementation("com.google.inject:guice:7.0.0")
    implementation("org.slf4j:slf4j-simple:2.0.16")
    implementation("net.engio:mbassador:1.3.2")
}

tasks.test {
    useJUnitPlatform()
}
