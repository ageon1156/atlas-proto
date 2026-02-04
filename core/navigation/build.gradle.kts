/*
 * Licensed under GPL-3.0
 */
import com.android.build.api.dsl.LibraryExtension

/*
 * Licensed under GPL-3.0
 */

plugins {
    alias(libs.plugins.meshtastic.android.library)
    alias(libs.plugins.meshtastic.kotlinx.serialization)
}

configure<LibraryExtension> { namespace = "org.meshtastic.core.navigation" }

dependencies { implementation(libs.kotlinx.serialization.core) }

