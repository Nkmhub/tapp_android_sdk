plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.example.tapprefenginesdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    kotlinOptions {
        jvmTarget = "11"
    }
}


dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    // Adjust (exclude the transitive signature range)
    api("com.adjust.sdk:adjust-android:5.0.0") {
        exclude(group = "com.adjust.signature", module = "adjust-android-signature")
    }
    api("com.adjust.sdk:adjust-android-webbridge:5.0.0") {
        exclude(group = "com.adjust.signature", module = "adjust-android-signature")
    }
    // ✅ Pin stable signature explicitly (prevents 3.48.0-alpha-SNAPSHOT)
    api("com.adjust.signature:adjust-android-signature") {
        version {
            strictly("3.47.0")              // enforce stable
            // (optional extra belt)
            // reject("3.48.0-.*")
            // reject(".*-SNAPSHOT")
        }
    }
    api("com.android.installreferrer:installreferrer:2.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.google.android.gms:play-services-ads-identifier:18.0.1")
}

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])

                groupId = "com.example.Nkmhub"
                artifactId = "tapp_ref_engine_sdk"
                version = "1.0.2"
            }
        }
    }
}



tasks.named("publishToMavenLocal") {
    dependsOn("assemble")
}