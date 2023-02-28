pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        kotlin("jvm").version(kotlinVersion).apply(false)
        kotlin("plugin.serialization").version(kotlinVersion).apply(false)
        id("org.jetbrains.compose").version(extra["compose.jb.version"] as String).apply(false)
    }
}
rootProject.name = "AppUpdater"
include(":updater")
