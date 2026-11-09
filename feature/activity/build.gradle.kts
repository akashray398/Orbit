plugins {
    id("orbit.android.library")
    id("orbit.android.compose")
    id("orbit.hilt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.orbit.app.feature.activity"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))
    implementation(project(":core:data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation("androidx.paging:paging-runtime:3.3.6")
    implementation("androidx.paging:paging-compose:3.3.6")
    implementation(libs.kotlinx.serialization.json)
}
