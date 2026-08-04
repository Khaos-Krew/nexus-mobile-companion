plugins {
    id("com.android.application")
}

fun String.asBuildConfigString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

val defaultUpdateManifestUrl =
    "https://raw.githubusercontent.com/Khaos-Krew/nexus-mobile-companion/main/updates/preview/update-manifest.json"
val defaultUpdatePublicKey =
    "MIIBojANBgkqhkiG9w0BAQEFAAOCAY8AMIIBigKCAYEAj2JV47xNe14bkc/R9L5g222Iu6UNys+S65W32yGZXMXcDqjFRQX1WGz95r+IsLpGOf1YPESi1x3RzsAmtcokzd4FEIiS9BGrrxqQ8gEZmCws3kaiC0RHYt92MNHnUv0ScdZOhSD+oPvnMzlisNdNzzbv0r7kTzcIYyo820lX6LMejU6t5le6ILl7TJZkzc9ebzng0JgsEZaKkTZa04Uwe9g2kZS8IjdhqqRqi7dK7bIH0hwmRH424iT2cGJMonLjeFuWYW5wli5P/264/C5pN0BTpXhg9/1oSRT4zSbqtcxKmXzT59NrgkVvfXcyjBrWcgJpCUj3kfpmJwtGgug/3BpdABvRj+vI8qmI2rlsx5AW/zYMhvg1NxLYo2qab/KvWJp7rE9geBpy3d+C9ZqnaZf6DzaZto1AE2c502E4jG3QCmowxmn/vXGxDpgW5e9y6GEh/2CpR62eNzbYL/JzApljzjg6ewkyQV6HZUXwDQf42DtwX3jwPRS11sqY7sbFAgMBAAE="
val defaultUpdateAllowedHosts = "raw.githubusercontent.com"

val updateManifestUrl = providers.environmentVariable("UPDATE_MANIFEST_URL")
    .orElse(defaultUpdateManifestUrl)
    .get()
val updatePublicKey = providers.environmentVariable("UPDATE_PUBLIC_KEY_B64")
    .orElse(defaultUpdatePublicKey)
    .get()
val updateAllowedHosts = providers.environmentVariable("UPDATE_ALLOWED_HOSTS")
    .orElse(defaultUpdateAllowedHosts)
    .get()
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
        versionCode = 5
        versionName = "0.2.3-preview"

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
        disable += "GestureBackNavigation"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}
