// <proje-root>/build.gradle.kts

plugins {
    // Sizin kullandığınız plugin’ler (apply false ile)
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android)     apply false

    // Firebase Google Services plugin’i
    id("com.google.gms.google-services")   version "4.4.2" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Eğer hâlen classpath ile yönetiyorsan:
        // classpath("com.google.gms:google-services:4.4.2")
    }
}


