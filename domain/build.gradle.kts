plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    // Datetime
    implementation(libs.kotlinx.datetime)

    implementation(libs.androidx.paging.common)
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)
}
