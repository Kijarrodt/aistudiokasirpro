plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

import java.security.KeyStore
import java.io.InputStream

android {
  namespace = "com.kasirpro.app"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.kasirpro.app"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.06066.1"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystoreFile = file("${rootDir}/my-upload-key.jks")
      val storePass = System.getenv("STORE_PASSWORD") 
        ?: "kasirpropass"
      val keyAliasValue = System.getenv("KEY_ALIAS") 
        ?: "upload"
      val keyPass = System.getenv("KEY_PASSWORD") 
        ?: "kasirpropass"

      if (keystoreFile.exists()) {
        storeFile = keystoreFile
        storePassword = storePass
        
        var finalAlias = keyAliasValue
        var finalKeyPassword = keyPass
        
        try {
          val keystore = KeyStore.getInstance(KeyStore.getDefaultType())
          val stream: InputStream = keystoreFile.inputStream()
          stream.use {
            keystore.load(it, storePass.toCharArray())
          }
          
          // Check if specified alias exists. If not, fallback to first available alias
          if (!keystore.containsAlias(keyAliasValue)) {
            val aliases = keystore.aliases()
            if (aliases.hasMoreElements()) {
              finalAlias = aliases.nextElement()
              println("Keystore Warning: Alias '$keyAliasValue' not found. Falling back to '$finalAlias'.")
            }
          }
          
          // Verify key password
          try {
            val key = keystore.getKey(finalAlias, keyPass.toCharArray())
            if (key == null) {
              // If key is null, try with store password as fallback
              val fallbackKey = keystore.getKey(finalAlias, storePass.toCharArray())
              if (fallbackKey != null) {
                finalKeyPassword = storePass
                println("Keystore Warning: Key password incorrect. Successfully fell back to store password.")
              }
            }
          } catch (e: Exception) {
            // Try store password fallback on exception
            try {
              val fallbackKey = keystore.getKey(finalAlias, storePass.toCharArray())
              if (fallbackKey != null) {
                finalKeyPassword = storePass
                println("Keystore Warning: Key password exception. Successfully fell back to store password.")
              }
            } catch (e2: Exception) {
              // keep original keyPass
            }
          }
        } catch (e: Exception) {
          // ignore keystore read failure, let gradle handle it or report it
        }
        
        keyAlias = finalAlias
        keyPassword = finalKeyPassword
      } else {
        // Keystore not found, skip release signing
        // Release build will be unsigned
        println("WARNING: Release keystore not found at ${keystoreFile.absolutePath}")
      }
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.auth)
  implementation(libs.firebase.firestore)
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services.auth)
  implementation(libs.googleid)
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.play.services)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
