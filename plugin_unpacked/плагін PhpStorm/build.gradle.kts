plugins {
    kotlin("jvm") version "1.9.24"
    id("org.jetbrains.intellij") version "1.17.3"
}

group = "com.splitwise.component"
version = "1.0.9"

repositories {
    mavenCentral()
}

intellij {
    type.set("PS")
    version.set("2023.3")
    plugins.set(listOf())
}

kotlin {
    jvmToolchain(11)
}

tasks {
    patchPluginXml {
        sinceBuild.set("233")
        untilBuild.set("253.*")
    }
}
