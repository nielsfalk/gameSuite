plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "gameSuite-pwa.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.html.core)
                implementation(compose.runtime)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
                implementation("io.kotest:kotest-assertions-core:5.8.1")
                implementation("io.kotest:kotest-framework-engine:5.8.1")
                implementation("io.kotest:kotest-framework-datatest:5.8.1")
            }
        }
    }
}
