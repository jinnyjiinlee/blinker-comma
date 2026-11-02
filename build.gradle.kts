plugins {
    id("com.android.application") version "8.7.3" apply false
    kotlin("android") version "2.0.21" apply false
    kotlin("plugin.compose") version "2.0.21" apply false
    kotlin("jvm") version "2.0.21" apply false
    kotlin("plugin.serialization") version "2.0.21" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.8" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2" apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.11" apply false
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(rootProject.file("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
    }
}

// 타입 분석 detekt(시계 금지·@Ignore 금지·!! 금지)도 check에 — 저장소만으로 규칙 전부 검사
subprojects {
    plugins.withId("org.jetbrains.kotlin.jvm") {
        tasks.named("check") { dependsOn("detektMain", "detektTest") }
    }
    plugins.withId("com.android.application") {
        tasks.named("check") { dependsOn("detektMain", "detektTest") }
    }
}
