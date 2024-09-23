plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
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
