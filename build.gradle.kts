import java.io.File

plugins {
    application
    java
}

repositories { mavenCentral() }

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

val os = org.gradle.internal.os.OperatingSystem.current()
val platform = when {
    os.isWindows -> "win"
    os.isMacOsX  -> if (System.getProperty("os.arch") == "aarch64") "mac-aarch64" else "mac"
    else         -> "linux"
}

dependencies {
    implementation("org.openjfx:javafx-base:21:$platform")
    implementation("org.openjfx:javafx-graphics:21:$platform")
    implementation("org.openjfx:javafx-controls:21:$platform")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("org.assertj:assertj-core:3.25.3")
}

tasks.test { useJUnitPlatform() }

application {
    // 기본 실행은 콘솔 앱 유지
    mainClass.set("hanja.ui.ConsoleApp")
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8")
}

tasks.withType<JavaCompile> { options.encoding = "UTF-8" }

tasks.named<JavaExec>("run") { standardInput = System.`in` }

/** GUI 실행 태스크: JavaFX 모듈패스/모듈 지정 */
tasks.register<JavaExec>("runGui") {
    group = "application"
    description = "Run JavaFX GUI"
    mainClass.set("hanja.gui.HanjaFXApp")
    classpath = sourceSets["main"].runtimeClasspath

    val fxModulePath = configurations.runtimeClasspath.get()
        .files
        .filter { it.name.startsWith("javafx-") }
        .joinToString(File.pathSeparator) { it.absolutePath }

    jvmArgs(
        "--module-path", fxModulePath,
        "--add-modules", "javafx.controls"
    )
}

/** 콘솔 전용 실행 */
tasks.register<JavaExec>("runConsole") {
    group = "application"
    description = "Run console UI"
    mainClass.set("hanja.ui.ConsoleApp")
    classpath = sourceSets["main"].runtimeClasspath
    standardInput = System.`in`
}