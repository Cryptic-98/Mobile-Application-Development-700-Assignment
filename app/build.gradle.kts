plugins {
    alias(libs.plugins.android.application)
}








val themealdbApiKey: String = (project.findProperty("themealdbApiKey") as String?)?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?: "1"
val themealdbBaseUrl: String = (project.findProperty("themealdbBaseUrl") as String?)?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?: "https://www.themealdb.com/api/json/v1/$themealdbApiKey/"

android {
    namespace = "com.example.smartpantrymanager"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.smartpantrymanager"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "THEMEALDB_BASE_URL", "\"$themealdbBaseUrl\"")
        buildConfigField("String", "THEMEALDB_API_KEY", "\"$themealdbApiKey\"")
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.recyclerview)
    implementation(libs.glide)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}