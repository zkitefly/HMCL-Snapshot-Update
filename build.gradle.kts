plugins {
    id("java")
    id("checkstyle")
    id("com.github.johnrengelman.shadow") version("7.0.0")
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

tasks.getByName("build") {
    dependsOn(tasks.getByName("checkstyleMain") {
        group = "build"
    })
    dependsOn(tasks.getByName("checkstyleTest") {
        group = "build"
    })
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.commons:commons-compress:1.23.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}