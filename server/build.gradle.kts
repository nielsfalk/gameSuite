plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    kotlin("plugin.power-assert")
    application
}

application {
    mainClass.set("de.nielsfalk.game.suite.server.ApplicationKt")
}

powerAssert {
    functions = listOf("kotlin.assert", "kotlin.test.assertTrue", "kotlin.test.assertEquals", "kotlin.test.assertNull")
}

dependencies {
    implementation(project(":shared"))

    val ktorVersion = "2.3.11"
    val exposedVersion = "0.50.1"

    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.5.6")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.h2database:h2:2.2.224")
    implementation("com.zaxxer:HikariCP:5.1.0")

    // Testing
    testImplementation("io.kotest:kotest-runner-junit5:5.8.1")
    testImplementation("io.kotest:kotest-assertions-core:5.8.1")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation-jvm:$ktorVersion")
    testImplementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

fun ProcessForkOptions.loadEnvFile(file: File) {
    if (!file.exists()) return
    file.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") && "=" in it }
        .forEach { line ->
            val (key, value) = line.split("=", limit = 2)
            environment(key.trim(), value.trim().trim('"', '\''))
        }
}

// Local secrets (e.g. Neon DATABASE_URL written by `neon init`) live in the git-ignored root .env.local.
// Only `run` and `databaseManualTest` load them; the regular `test` task keeps using in-memory H2.
tasks.named<JavaExec>("run") {
    loadEnvFile(rootProject.file(".env.local"))
}

tasks.register<Test>("databaseManualTest") {
    group = "verification"
    description = "Runs DatabaseManualTest against DATABASE_URL/DB_URL from .env.local or the environment"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform()
    filter {
        includeTestsMatching("de.nielsfalk.game.suite.server.DatabaseManualTest")
    }
    loadEnvFile(rootProject.file(".env.local"))
}
