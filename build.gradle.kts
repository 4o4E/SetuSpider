import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
}

group = "com.e404"
version = "1.0.0"

val log4jVersion = "2.20.0"
val ktorVersion = "2.2.3"
fun log4j(module: String) = "org.apache.logging.log4j:log4j-$module:$log4jVersion"
fun ktor(module: String) = "io.ktor:ktor-$module:$ktorVersion"

repositories {
    mavenCentral()
}

dependencies {
    // jsoup
    implementation("org.jsoup:jsoup:1.14.3")
    // gson
    implementation("com.google.code.gson:gson:2.8.9")
    // coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.2")
    // ktor
    implementation(ktor("client-core"))
    implementation(ktor("client-cio"))

    // slf4j
    implementation("org.slf4j:slf4j-api:1.7.32")
    // 适配器
    runtimeOnly(log4j("slf4j-impl"))
    // log4j2
    runtimeOnly(log4j("core"))
    runtimeOnly(log4j("api"))
    // 异步
    implementation("com.lmax:disruptor:3.4.4")

    // test
    testImplementation(kotlin("test", "1.8.0"))
}

tasks.test {
    useJUnit()
    workingDir = File("run")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}