import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    jacoco
}

// Configuracion de la firma (solo uso academico). Ver keystore.properties y el README.
val keystoreProperties = Properties().apply {
    val file = rootProject.file("keystore.properties")
    if (file.exists()) file.inputStream().use { load(it) }
}

val authBaseUrl = (project.findProperty("authBaseUrl") as String?) ?: "http://10.0.2.2:8081/"
val ticketsBaseUrl = (project.findProperty("ticketsBaseUrl") as String?) ?: "http://10.0.2.2:8082/"

android {
    namespace = "com.panalbit.panalsuite"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.panalbit.panalsuite"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "AUTH_BASE_URL", "\"$authBaseUrl\"")
        buildConfigField("String", "TICKETS_BASE_URL", "\"$ticketsBaseUrl\"")
        buildConfigField("String", "INDICATORS_BASE_URL", "\"https://mindicador.cl/\"")
    }

    signingConfigs {
        create("release") {
            storeFile = rootProject.file(keystoreProperties.getProperty("storeFile", "keystore/panalsuite-academic.jks"))
            storePassword = keystoreProperties.getProperty("storePassword")
            keyAlias = keystoreProperties.getProperty("keyAlias")
            keyPassword = keystoreProperties.getProperty("keyPassword")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            enableUnitTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Almacenamiento local: Room (cache de tickets y evidencias) + DataStore (sesion)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)

    // Red: Retrofit (microservicios propios + API externa)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.android)

    // Pruebas unitarias
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}

// ---------------------------------------------------------------------------------------------
// Cobertura (JaCoCo) solo de la logica: repositorios, ViewModels, reglas, validadores y utilidades.
// Se excluye la UI de Compose, el contenedor de dependencias y las clases generadas.
// ---------------------------------------------------------------------------------------------
tasks.withType<Test>().configureEach {
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

val coverageExcludes = listOf(
    "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
    "**/ui/components/**", "**/ui/theme/**", "**/ui/navigation/**", "**/ui/**/*Screen*", "**/ui/**/*Tab*",
    "**/util/EvidenceFiles*", "**/di/**", "**/MainActivity*", "**/PanalSuiteApp*",
    "**/*_Impl*", "**/AppDatabase*", "**/DataStoreSessionStorage*", "**/*Factory*",
    "**/*\$\$serializer*", "**/ComposableSingletons*"
)

tasks.register<JacocoReport>("jacocoLogicReport") {
    group = "verification"
    description = "Reporte de cobertura de la logica (sin UI)"
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) { exclude(coverageExcludes) }
    )
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include("jacoco/testDebugUnitTest.exec", "outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec")
        }
    )
}
