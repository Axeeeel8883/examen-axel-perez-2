plugins {
 kotlin("multiplatform"); kotlin("plugin.serialization"); kotlin("plugin.compose")
 id("com.android.library"); id("org.jetbrains.compose"); id("app.cash.sqldelight")
}
kotlin {
 androidTarget(); jvm()
 jvmToolchain(21)
 sourceSets {
  commonMain.dependencies {
   implementation(compose.runtime); implementation(compose.foundation); implementation(compose.material3)
   implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:2.8.4")
   implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
   implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
   implementation("io.insert-koin:koin-core:4.0.4")
   implementation("io.ktor:ktor-client-core:3.1.3")
   implementation("io.ktor:ktor-client-content-negotiation:3.1.3")
   implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.3")
   implementation("io.ktor:ktor-client-logging:3.1.3")
   implementation("app.cash.sqldelight:runtime:2.1.0")
   implementation("app.cash.sqldelight:coroutines-extensions:2.1.0")
  }
  androidMain.dependencies {
   implementation("io.ktor:ktor-client-okhttp:3.1.3")
   implementation("app.cash.sqldelight:android-driver:2.1.0")
  }
  commonTest.dependencies {
   implementation(kotlin("test")); implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
   implementation("io.ktor:ktor-client-mock:3.1.3")
  }
  jvmTest.dependencies { implementation("app.cash.sqldelight:sqlite-driver:2.1.0") }
 }
}
android { namespace="pe.upeu.agrosierra.shared"; compileSdk=36
 defaultConfig { minSdk=24 }
 compileOptions { sourceCompatibility=JavaVersion.VERSION_21; targetCompatibility=JavaVersion.VERSION_21 }
}
sqldelight { databases { create("AgroDb") { packageName.set("pe.upeu.agrosierra.db") } } }

