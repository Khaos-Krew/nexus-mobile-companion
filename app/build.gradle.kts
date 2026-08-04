plugins {
    id("com.android.application")
}

fun String.asBuildConfigString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

val updateManifestUrl = providers.environmentVariable("UPDATE_MANIFEST_URL").orElse("").get()
val updatePublicKey = providers.environmentVariable("UPDATE_PUBLIC_KEY_B64").orElse("").get()
val updateAllowedHosts = providers.environmentVariable("UPDATE_ALLOWED_HOSTS").orElse("").get()
val updateChannel = providers.environmentVariable("UPDATE_CHANNEL").orElse("preview").get()

val previewKeystorePath = providers.environmentVariable("PREVIEW_KEYSTORE_PATH").orNull
val previewKeystorePassword = providers.environmentVariable("PREVIEW_KEYSTORE_PASSWORD").orNull
val previewKeyAlias = providers.environmentVariable("PREVIEW_KEY_ALIAS").orNull
val previewKeyPassword = providers.environmentVariable("PREVIEW_KEY_PASSWORD").orNull
val hasStablePreviewSigning = listOf(
    previewKeystorePath,
    previewKeystorePassword,
    previewKeyAlias,
    previewKeyPassword
).all { !it.isNullOrBlank() }

android {
    namespace = "com.khaoskrew.nexuscompanion"
    compileSdk = 36
    enableKotlin = false

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.khaoskrew.nexuscompanion"
        minSdk = 26
        targetSdk = 36
        versionCode = 2
        versionName = "0.2.0-preview"

        buildConfigField("String", "UPDATE_MANIFEST_URL", updateManifestUrl.asBuildConfigString())
        buildConfigField("String", "UPDATE_PUBLIC_KEY_B64", updatePublicKey.asBuildConfigString())
        buildConfigField("String", "UPDATE_ALLOWED_HOSTS", updateAllowedHosts.asBuildConfigString())
        buildConfigField("String", "UPDATE_CHANNEL", updateChannel.asBuildConfigString())
        buildConfigField("boolean", "UPDATE_CHECK_ENABLED", (!updateManifestUrl.isBlank()).toString())
        buildConfigField("boolean", "STABLE_PREVIEW_SIGNING", hasStablePreviewSigning.toString())
    }

    signingConfigs {
        if (hasStablePreviewSigning) {
            create("preview") {
                storeFile = file(previewKeystorePath!!)
                storePassword = previewKeystorePassword
                keyAlias = previewKeyAlias
                keyPassword = previewKeyPassword
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = true
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".preview"
            versionNameSuffix = "-debug"
            if (hasStablePreviewSigning) {
                signingConfig = signingConfigs.getByName("preview")
            }
        }
        getByName("release") {
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

    lint {
        abortOnError = true
        checkDependencies = true
        checkReleaseBuilds = false
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
