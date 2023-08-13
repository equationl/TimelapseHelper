import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.equationl"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "TimelapseHelper"
            packageVersion = "1.0.0"
        }
    }
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("com.drewnoakes:metadata-extractor:2.18.0")
    // implementation("com.drewnoakes:metadata-extractor:2.18.0")
}
