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
}

kotlin {
    jvmToolchain(21)
}