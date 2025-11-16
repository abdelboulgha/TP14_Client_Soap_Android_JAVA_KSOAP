plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.tp14_client_soap_android"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.tp14_client_soap_android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // KSoap2-android avec exclusions des dépendances problématiques
    implementation("com.github.simpligility:ksoap2-android:3.6.4") {
        exclude(group = "net.sourceforge.kxml", module = "kxml")
        exclude(group = "net.sourceforge.kobjects", module = "kobjects-j2me")
        exclude(group = "net.sourceforge.me4se", module = "me4se")
        // Exclure ksoap2-okhttp pour éviter les classes dupliquées
        exclude(group = "com.github.simpligility.ksoap2-android", module = "ksoap2-okhttp")
    }

    // Ajouter kxml2 qui est la version moderne et compatible
    implementation("net.sf.kxml:kxml2:2.3.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}