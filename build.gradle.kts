plugins {
    java
    checkstyle
    id("com.github.johnrengelman.shadow") version ("7.1.2")
}

group = "net.burningtnt"
version = "1.0-SNAPSHOT"
description = "A tool to automatically download artifacts from GitHub Action and upload them to current repository."

repositories {
    mavenCentral()
}

checkstyle {
    sourceSets = mutableSetOf()
}

tasks.build {
    dependsOn(tasks.checkstyleMain {
        group = "build"
    })
    dependsOn(tasks.checkstyleTest {
        group = "build"
    })
    dependsOn(tasks.shadowJar {
        manifest {
            attributes(
                    "Main-Class" to "net.burningtnt.hmclfetcherx    .Main"
            )
        }
    })
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.commons:commons-compress:1.24.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}