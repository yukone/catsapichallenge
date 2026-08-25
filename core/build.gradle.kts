import com.android.build.api.variant.BuildConfigField
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    id("com.google.devtools.ksp")
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "tv.bae.core"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

val keystoreFile = project.rootProject.file("apikey.properties")
val properties = Properties()
properties.load(keystoreFile.inputStream())
val apiKey = properties.getProperty("CAT_API_KEY") ?: ""

androidComponents {
    onVariants { variant ->
        variant.buildConfigFields?.put(
            "CAT_API_KEY",
            BuildConfigField(
                type = "String",
                value = "\"${apiKey}\"",
                comment = null
            )
        )
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    implementation(libs.ktor.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.serialization.json)
    implementation(libs.serialization.json)

    implementation(libs.room.runtime)
    ksp(libs.room.compiler)

    implementation(libs.koin.core)

    testImplementation(libs.junit)
}