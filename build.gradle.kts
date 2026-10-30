plugins {
    kotlin("multiplatform") version "2.0.0" apply false
    kotlin("jvm") version "2.0.0" apply false
    kotlin("plugin.serialization") version "2.0.0" apply false
    kotlin("plugin.power-assert") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    id("org.jetbrains.compose") version "1.6.11" apply false
    id("io.ktor.plugin") version "2.3.11" apply false
    id("io.github.nielsfalk.datatable") version "0.2.1" apply false
}

allprojects {
    group = "de.nielsfalk.game.suite"
    version = "1.0.0-SNAPSHOT"
}
