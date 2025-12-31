plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
}

group = "io.github.fuyuz.svgicon"
version = "0.1.0-SNAPSHOT"

dependencies {
    implementation(libs.kotlinpoet)

    // Gradle API
    compileOnly(gradleApi())
}

kotlin {
    jvmToolchain(17)
}

gradlePlugin {
    plugins {
        create("svgIcon") {
            id = "io.github.fuyuz.svgicon"
            implementationClass = "io.github.fuyuz.svgicon.gradle.SvgIconPlugin"
        }
    }
}
