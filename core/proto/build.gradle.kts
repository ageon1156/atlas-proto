/*
 * Licensed under GPL-3.0
 */
import com.android.build.api.dsl.LibraryExtension

/*
 * Licensed under GPL-3.0
 */
plugins {
    alias(libs.plugins.meshtastic.android.library)
    alias(libs.plugins.meshtastic.android.library.compose)
    alias(libs.plugins.protobuf)
}

configure<LibraryExtension> { namespace = "org.meshtastic.core.proto" }

// per protobuf-gradle-plugin docs, this is recommended for android
protobuf {
    protoc { artifact = libs.protobuf.protoc.get().toString() }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {}
                create("kotlin") {}
            }
        }
    }
}

dependencies {
    // This needs to be API for consuming modules
    api(libs.protobuf.kotlin)
}

