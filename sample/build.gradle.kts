// 쉼표 샘플 — 엔진(:engine)만 가져다 쓰는 두 번째 앱. 문장마다 파일을 자르지 않고 긴 음원 하나 + 자막(VTT) 경계로 동작한다
// (앱마켓 오디오·팟캐스트 앱이 쉼표 규칙을 붙이는 방식 — docs/INTEGRATION.md 2절 「스트리밍하는 앱」).
plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "app.blinkercomma.sample"
    compileSdk = 34

    defaultConfig {
        applicationId = "app.blinkercomma.sample"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint { abortOnError = true }

    testOptions { unitTests.all { it.useJUnitPlatform() } }
}

kotlin {
    jvmToolchain(21)
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

dependencies {
    implementation(project(":engine"))
    implementation("ai.pleos.playground:Vehicle:2.3.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:2.0.21")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}
