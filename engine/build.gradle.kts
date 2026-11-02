import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.kotlinx.kover")
}

// 툴체인은 21, 바이트코드는 :app과 같은 17
kotlin {
    jvmToolchain(21)
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

java {
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    // 1.7.3 고정 — 1.8.0은 stdlib를 2.1.0으로 올리고 1.9.0은 Kotlin 2.0.21에서 컴파일 실패
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    testImplementation(kotlin("test"))
}

// 무대 대본(demo/script)을 시나리오 테스트가 그대로 읽는다 — 대본과 테스트가 갈라지지 않게
sourceSets.test {
    resources.srcDir(rootProject.file("demo/script"))
}

tasks.test {
    useJUnitPlatform()
}

// 커버리지는 바닥 — 경계는 「경계 3점」 테스트가 맡는다
kover {
    reports {
        verify {
            rule("engine 줄 커버리지") { minBound(90) }
            rule("engine 분기 커버리지") {
                minBound(85, kotlinx.kover.gradle.plugin.dsl.CoverageUnit.BRANCH)
            }
        }
    }
}
