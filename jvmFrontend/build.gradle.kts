plugins {
    alias(libs.plugins.kotlin.jvm)
}

group = "invalid.sergonezero.corvirc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlinx.io.core)
}

kotlin {
    jvmToolchain(21)
}