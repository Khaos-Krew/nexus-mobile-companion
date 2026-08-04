plugins {
    id("com.android.application")
}

android {
    namespace = "com.khaoskrew.nexuscompanion"
    compileSdk = 37
    enableKotlin = false

    defaultConfig {
        applicationId = "com.khaoskrew.nexuscompanion"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0-preview"
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".preview"
            versionNameSuffix = "-debug"
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
