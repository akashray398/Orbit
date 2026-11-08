plugins {
    id("orbit.android.library")
    id("orbit.android.compose")
    id("orbit.hilt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.orbit.app.feature.onboarding"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation("androidx.compose.material:material-icons-extended")
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
