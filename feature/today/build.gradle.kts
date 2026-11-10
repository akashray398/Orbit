plugins {
    id("orbit.android.library")
    id("orbit.android.compose")
    id("orbit.hilt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.orbit.app.feature.today"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)
}
