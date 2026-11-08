import com.android.build.api.dsl.CommonExtension

plugins {
    id("org.jetbrains.kotlin.plugin.compose")
}

val androidExtension = extensions.getByType(CommonExtension::class.java)
androidExtension.apply {
    buildFeatures {
        compose = true
    }
}

dependencies {
    add("implementation", platform("androidx.compose:compose-bom:2025.02.00"))
    add("implementation", "androidx.compose.ui:ui")
    add("implementation", "androidx.compose.ui:ui-graphics")
    add("implementation", "androidx.compose.ui:ui-tooling-preview")
    add("implementation", "androidx.compose.material3:material3")
    add("debugImplementation", "androidx.compose.ui:ui-tooling")
}
