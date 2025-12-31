import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    id("io.github.fuyuz.svgicon")
}

svgIcon {
    svgDir.set(file("src/commonMain/composeResources/svg"))
    packageName.set("io.github.fuyuz.svgicon.sample.generated.icons")
    visibility.set("public")
}

kotlin {
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir(layout.buildDirectory.dir("generated/compose/resourceGenerator/kotlin/svgicons"))
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.components.uiToolingPreview)
                implementation(project(":runtime"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.fuyuz.svgicon.sample.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "svg-icon-sample"
            packageVersion = "1.0.0"
        }
    }
}
