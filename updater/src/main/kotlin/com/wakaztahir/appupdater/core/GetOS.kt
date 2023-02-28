package com.wakaztahir.appupdater.core

enum class OS(val supportedExtensions : List<String>,val supportNoExtension : Boolean) {
    WINDOWS(listOf("exe","msi"),false),
    MAC(listOf("dmg","pkg"),true),
    LINUX(listOf("deb","rpm"),true)
}

fun getOS(): OS? {
    val osName = System.getProperty("os.name").lowercase()
    return when {
        osName.contains("win") -> OS.WINDOWS
        osName.contains("mac") || osName.contains("darwin") -> OS.MAC
        osName.contains("nux") || osName.contains("nix") -> OS.LINUX
        else -> null
    }
}