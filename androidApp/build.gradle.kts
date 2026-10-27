plugins { id("com.android.application"); kotlin("android"); kotlin("plugin.compose") }
android {
 namespace="pe.upeu.agrosierra"; compileSdk=36
 defaultConfig { applicationId="pe.upeu.agrosierra"; minSdk=24; targetSdk=35; versionCode=1; versionName="1.0" }
 compileOptions { sourceCompatibility=JavaVersion.VERSION_21; targetCompatibility=JavaVersion.VERSION_21 }
 buildFeatures { compose=true; buildConfig=true }
 signingConfigs { getByName("debug") { storeFile=rootProject.file("debug.keystore");storePassword="android";keyAlias="androiddebugkey";keyPassword="android" } }
 buildTypes { debug { isDebuggable=true }; release { isMinifyEnabled=false } }
 packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
kotlin { jvmToolchain(21) }
dependencies {
 implementation(project(":shared"))
 implementation("androidx.activity:activity-compose:1.10.1")
 implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
 implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
 implementation("io.insert-koin:koin-android:4.0.4")
 implementation("app.cash.sqldelight:android-driver:2.1.0")
 implementation("io.ktor:ktor-client-okhttp:3.1.3")
}



val prepararFirmaDebug by tasks.registering(Exec::class) {
 val firma=rootProject.file("debug.keystore")
 onlyIf { !firma.exists() }
 val ejecutable=if(System.getProperty("os.name").startsWith("Windows"))"keytool.exe" else "keytool"
 commandLine("${System.getProperty("java.home")}/bin/$ejecutable", "-genkeypair", "-keystore",firma.absolutePath,
  "-storepass","android","-keypass","android","-alias","androiddebugkey",
  "-dname","CN=Android Debug,O=Android,C=PE","-keyalg","RSA","-validity","10000")
}
tasks.matching { it.name=="validateSigningDebug" }.configureEach { dependsOn(prepararFirmaDebug) }
