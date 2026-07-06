// Top-level build file
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
}

allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension>("detekt") {
        config.setFrom(files("$rootDir/detekt.yml"))
        baseline = file("detekt-baseline.xml")
        buildUponDefaultConfig = true
        allRules = false
        parallel = true
    }

    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension>("ktlint") {
        android.set(true)
        outputToConsole.set(true)
        ignoreFailures.set(true)
        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
        }
    }
}
