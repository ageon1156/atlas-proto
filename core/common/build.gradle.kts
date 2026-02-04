/*
 * Licensed under GPL-3.0
 */

plugins { alias(libs.plugins.meshtastic.kmp.library) }

kotlin {
    @Suppress("UnstableApiUsage")
    androidLibrary {}

    sourceSets { androidMain.dependencies { implementation(libs.androidx.core.ktx) } }
}

