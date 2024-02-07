buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        classpath ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
    }
        repositories {
            gradlePluginPortal()
            google()
            mavenCentral()
        }
}

plugins {
    id("com.android.application") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id ("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")  version "2.0.1" apply false
}