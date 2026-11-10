plugins {
    id("orbit.android.library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.orbit.app.eval"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))
    implementation(project(":core:testing"))

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
