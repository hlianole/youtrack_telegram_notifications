plugins {
    kotlin("jvm") version "2.1.10"
    application
    kotlin("plugin.serialization") version "1.4.21"
}

group = "com.hlianole.jetbrains.internship"
version = "1.0.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {

    // Kotlin Telegram Bot
    // https://mvnrepository.com/artifact/io.github.kotlin-telegram-bot.kotlin-telegram-bot/telegram
    implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.3.0")

    // Serialization
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-serialization-json
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // Ktor
    // https://mvnrepository.com/artifact/io.ktor/ktor-client-core
    implementation("io.ktor:ktor-client-core:3.3.1")
    // https://mvnrepository.com/artifact/io.ktor/ktor-client-cio
    implementation("io.ktor:ktor-client-cio:3.3.1")
    // https://mvnrepository.com/artifact/io.ktor/ktor-client-content-negotiation
    implementation("io.ktor:ktor-client-content-negotiation:3.3.1")
    // https://mvnrepository.com/artifact/io.ktor/ktor-serialization-kotlinx-json
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.1")
    implementation("io.ktor:ktor-client-cio-jvm:3.3.1")

    // Config
    // https://mvnrepository.com/artifact/com.typesafe/config
    implementation("com.typesafe:config:1.4.3")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.hlianole.jetbrains.internship.youtrack_telegram.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(
        configurations.runtimeClasspath.get().map {
            if (it.isDirectory) {
                it
            }
            else {
                zipTree(it)
            }
        }
    )
}

tasks.test {
    useJUnitPlatform()
}
