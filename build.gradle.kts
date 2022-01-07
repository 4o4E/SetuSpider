import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
}

group = "com.e404"
version = "1.0.0"
val log4jVersion = "2.17.1"
fun log4j(module: String) = "org.apache.logging.log4j:log4j-$module:$log4jVersion"

repositories {
    mavenCentral()
}

dependencies {
    // jsoup
    implementation("org.jsoup:jsoup:1.14.3")
    // gson
    implementation("com.google.code.gson:gson:2.8.9")
    // slf4j
    api("org.slf4j:slf4j-api:1.7.32")
    // 适配器
    runtimeOnly(log4j("slf4j-impl"))
    // log4j2
    runtimeOnly(log4j("core"))
    runtimeOnly(log4j("api"))
    // 异步
    implementation("com.lmax:disruptor:3.4.4")
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}