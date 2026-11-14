plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

val detectedGithubRepo: String = runCatching {
    val process = ProcessBuilder("git", "remote", "get-url", "origin")
        .directory(rootProject.projectDir)
        .redirectErrorStream(true)
        .start()
    val url = process.inputStream.bufferedReader().readText().trim()
    val exited = process.waitFor()
    if (exited == 0 && url.contains("github.com", ignoreCase = true)) {
        url
            .replace(Regex("^https?://github\\.com/"), "")
            .replace(Regex("^git@github\\.com:"), "")
            .removeSuffix(".git")
            .trim('/')
    } else ""
}.getOrDefault("")

val wuppGithubRepo: String = (
    (project.findProperty("WUPP_GITHUB_REPO") as? String)?.takeIf { detectedGithubRepo.isBlank() && it.contains("/") }
        ?: detectedGithubRepo.ifBlank {
            (project.findProperty("WUPP_GITHUB_REPO") as? String).orEmpty().ifBlank { "hanzalaramz/Wupp" }
        }
    ).replace("\"", "")

val wuppVersionCode: Int = (
    System.getenv("WUPP_VERSION_CODE")
        ?: (project.findProperty("WUPP_VERSION_CODE") as? String)
    )?.toIntOrNull() ?: 1

val wuppVersionName: String = (
    System.getenv("WUPP_VERSION_NAME")
        ?: (project.findProperty("WUPP_VERSION_NAME") as? String)
    )?.trim()?.removePrefix("v")?.takeIf { it.isNotBlank() } ?: "1.0.0"

android {
    namespace = "com.wa.beastclient"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.wa.beastclient"
        minSdk = 26
        targetSdk = 34
        versionCode = wuppVersionCode
        versionName = wuppVersionName

        buildConfigField("String", "GITHUB_REPO", "\"$wuppGithubRepo\"")

        ndk {
            abiFilters += "arm64-v8a"
        }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.generateKotlin", "true")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(files("libs/whatsbridge.aar"))

    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-service:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.3")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("androidx.room:room-paging:2.8.5")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.paging:paging-runtime:3.3.2")
    implementation("androidx.paging:paging-compose:3.3.2")

    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-common:1.4.1")

    implementation("com.airbnb.android:lottie-compose:6.4.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
