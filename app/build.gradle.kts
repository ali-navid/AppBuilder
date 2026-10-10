import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// --- پارامترهای هر اپ (از خط فرمان -P… یا gradle.properties قابل override؛ وگرنه پیش‌فرض) ---
val appId      = (project.findProperty("APP_ID") as String?) ?: "com.parsbit.online"
val appName    = (project.findProperty("APP_NAME") as String?) ?: "پارس بیت"
val appVerCode = ((project.findProperty("APP_VERSION_CODE") as String?)?.toIntOrNull()) ?: 5
val appVerName = (project.findProperty("APP_VERSION_NAME") as String?) ?: "1.0.5"

android {
    namespace = "com.parsbit.online"   // ثابت می‌ماند (فقط پکیجِ کلاس R؛ به شناسهٔ نصب ربطی ندارد)
    compileSdk = 36

    defaultConfig {
        applicationId = appId
        minSdk = 24
        targetSdk = 36                 // ≥۳۴ موردنیاز بازار
        versionCode = appVerCode
        versionName = appVerName
        manifestPlaceholders["appLabel"] = appName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // کلید امضای release: از keystore.properties (محلی) یا متغیرهای محیطی (CI)
    val keystorePropsFile = rootProject.file("keystore.properties")
    val hasKeystoreFile = keystorePropsFile.exists()
    val keystoreProps = Properties().apply { if (hasKeystoreFile) load(FileInputStream(keystorePropsFile)) }
    val envStore = System.getenv("KEYSTORE_FILE")

    signingConfigs {
        create("release") {
            when {
                hasKeystoreFile -> {
                    storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                    storePassword = keystoreProps.getProperty("storePassword")
                    keyAlias = keystoreProps.getProperty("keyAlias")
                    keyPassword = keystoreProps.getProperty("keyPassword")
                }
                envStore != null -> {
                    storeFile = file(envStore)
                    storePassword = System.getenv("KEYSTORE_PASSWORD")
                    keyAlias = System.getenv("KEY_ALIAS")
                    keyPassword = System.getenv("KEY_PASSWORD")
                }
            }
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = if (hasKeystoreFile || envStore != null) signingConfigs.getByName("release") else null
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { buildConfig = true }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.2.0-beta01")
    implementation("androidx.webkit:webkit:1.17.1")
}
