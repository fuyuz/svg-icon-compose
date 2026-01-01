import com.vanniktech.maven.publish.GradlePlugin
import com.vanniktech.maven.publish.JavadocJar

plugins {
    alias(libs.plugins.kotlin.jvm)
    `java-gradle-plugin`
    alias(libs.plugins.maven.publish)
}

group = "io.github.fuyuz.svgicon"

dependencies {
    implementation(libs.kotlinpoet)

    // Gradle API
    compileOnly(gradleApi())

    // Kotlin Gradle Plugin API for KotlinCompilationTask
    compileOnly(libs.kotlin.gradle.plugin)

    // Test dependencies
    testImplementation(kotlin("test"))
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

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    configure(GradlePlugin(javadocJar = JavadocJar.Javadoc()))

    coordinates(group.toString(), "gradle-plugin")

    pom {
        name.set("SVG Icon Compose Gradle Plugin")
        description.set("Gradle plugin for generating SVG icon accessors in Compose Multiplatform projects")
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
