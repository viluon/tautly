import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.20"
    application
}

group = "me.viluon"
version = "0.0.1"

val lwjglVersion = "3.2.3"
val kotestVersion = "4.3.0.700-SNAPSHOT"
val kotlinxCoroutinesVersion = "1.4.2"

application {
    mainClassName = "MainKt"
}

@Suppress("INACCESSIBLE_TYPE")
val lwjglNatives = when (OperatingSystem.current()) {
    OperatingSystem.LINUX   -> "natives-linux"
    OperatingSystem.MAC_OS  -> "natives-macos"
    OperatingSystem.WINDOWS -> "natives-windows"
    else -> throw Error("Unrecognized or unsupported operating system. Please set \"lwjglNatives\" manually.")
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion") // for kotest framework
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion") // for kotest core jvm assertions
    testImplementation("io.kotest:kotest-property:$kotestVersion") // for kotest property test
    testImplementation(kotlin("test-junit"))

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")

    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-nanovg")
    implementation("org.lwjgl", "lwjgl-openal")
    implementation("org.lwjgl", "lwjgl-opengl")
    implementation("org.lwjgl", "lwjgl-stb")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-nanovg", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-openal", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-stb", classifier = lwjglNatives)
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
