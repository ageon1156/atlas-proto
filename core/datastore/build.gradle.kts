/*
 * Licensed under GPL-3.0
 */
import com.android.build.api.dsl.LibraryExtension

/*
 * Licensed under GPL-3.0
 */

plugins {
    alias(libs.plugins.meshtastic.android.library)
    alias(libs.plugins.meshtastic.hilt)
    alias(libs.plugins.meshtastic.kotlinx.serialization)
}

configure<LibraryExtension> { namespace = "org.meshtastic.core.datastore" }

dependencies {
    implementation(projects.core.proto)

    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kermit)
}

