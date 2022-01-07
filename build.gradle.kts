import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
}

group = "com.e404"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // jsoup
    implementation("org.jsoup:jsoup:1.14.3")
    // gson
    implementation("com.google.code.gson:gson:2.8.9")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}