plugins {
    kotlin("multiplatform") version "1.9.23" apply false
    kotlin("jvm") version "1.9.23" apply false
    kotlin("plugin.serialization") version "1.9.23" apply false
    id("org.jetbrains.compose") version "1.6.11" apply false
    id("io.ktor.plugin") version "2.3.11" apply false
}

allprojects {
    group = "de.nielsfalk.game.suite"
    version = "1.0.0-SNAPSHOT"
}
