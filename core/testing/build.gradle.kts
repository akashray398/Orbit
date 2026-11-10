plugins {
    id("orbit.android.library")
}

android {
    namespace = "com.orbit.app.core.testing"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:common"))

    implementation(libs.junit)
    implementation(libs.kotlinx.coroutines.test)
}
