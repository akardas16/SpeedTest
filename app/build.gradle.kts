plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
    id("kotlinx-serialization")
}

android {
    namespace = "com.akardas16.networkspeed"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.akardas16.networkspeed"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(project(":speedtest"))

    implementation(libs.jsoup)
    implementation(libs.android.maps.utils)
    //Location
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.places)

    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)

    //Coil && landscapist libraries for images --> Check for detail https://github.com/skydoves/landscapist
    implementation(libs.landscapist.coil)
    implementation(libs.coil.compose)
    implementation(libs.landscapist.transformation)
    implementation(libs.landscapist.placeholder)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    implementation(libs.compose.image)
    implementation(libs.flow)


    //InApp Purchase Billing dependencies
    // implementation(project(":google-iab"))
    implementation(libs.google.inapp.billing)



    implementation(libs.glide)


// Room Local DB
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    annotationProcessor(libs.androidx.room.compiler)
    //kapt(libs.androidx.room.compiler.kapt)


    //VOYAGER
    implementation(libs.voyager.navigator) // Navigator
    implementation(libs.voyager.screenmodel) // Screen Model
    implementation(libs.voyager.bottom.sheet.navigator) // BottomSheetNavigator
    implementation(libs.voyager.tab.navigator) // TabNavigator
    implementation(libs.voyager.transitions) // Transitions
    implementation(libs.voyager.livedata) // LiveData integration
    // Koin integration
    implementation(libs.voyager.koin)
    // Koin Core features
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)


    //Lottie dependency
    implementation(libs.lottie.compose)

    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.gson)
    implementation(libs.alerter)
    debugImplementation(libs.ui.tooling)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.material3)
    // implementation(libs.androidx.compose.material)
    implementation(libs.androidx.material)
    //implementation(libs.androidx.material.icons.extended)


    implementation(libs.compose)

    //Ktor libraries
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.serialization)
    implementation(libs.ktor.serialization.kotlinx.json)

    //SplashScreen
    implementation(libs.androidx.core.splashscreen)


    implementation(libs.androidx.activity)
    //ViewModels delegation extentensions for activity
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.ui.util)



    implementation(libs.androidx.navigation.runtime.ktx)





    implementation(libs.androidx.navigation.compose)

    // implementation(libs.ycharts)

    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.fragment.ktx)



    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}