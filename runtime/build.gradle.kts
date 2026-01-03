import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kover)
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xcontext-parameters")
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
        publishLibraryVariants("release")
    }

    jvm("desktop") {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SvgIconRuntime"
            isStatic = true
        }
    }

    js(IR) {
        browser()
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material3)
            implementation(compose.components.uiToolingPreview)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        val androidMain by getting {
            dependencies {
                implementation(compose.uiTooling)
            }
        }
    }
}

android {
    namespace = "io.github.fuyuz.svgicon.runtime"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kover {
    reports {
        filters {
            excludes {
                // Exclude SvgIcon.kt composables - requires UI testing framework
                classes(
                    "io.github.fuyuz.svgicon.SvgIcon*",
                    "io.github.fuyuz.svgicon.Animated*",
                    "io.github.fuyuz.svgicon.PathRenderer*",
                    "io.github.fuyuz.svgicon.ComposableSingletons*"
                )
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(group.toString(), "runtime")

    pom {
        name.set("SVG Icon Compose Runtime")
        description.set("Kotlin Multiplatform library for SVG icons in Jetpack Compose")
        url.set("https://github.com/fuyuz/svg-icon-compose")
        inceptionYear.set("2026")

        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        developers {
            developer {
                id.set("fuyuz")
                name.set("fuyuz")
                url.set("https://github.com/fuyuz")
            }
        }

        scm {
            url.set("https://github.com/fuyuz/svg-icon-compose")
            connection.set("scm:git:git://github.com/fuyuz/svg-icon-compose.git")
            developerConnection.set("scm:git:ssh://git@github.com/fuyuz/svg-icon-compose.git")
        }
    }
}
