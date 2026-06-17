plugins {
    alias(libs.plugins.convention.cmp.application)
}

kotlin {
    
    androidLibrary {
       namespace = "com.chatapp.chirp.shared"
       compileSdk = libs.versions.projectCompileSdkVersion.get().toInt()
       minSdk = libs.versions.projectMinSdkVersion.get().toInt()

       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
        }
        commonMain.dependencies {
            implementation(projects.core.data)
            implementation(projects.core.domain)
            implementation(projects.core.designsystem)
            implementation(projects.core.presentation)

            implementation(projects.feature.auth.domain)
            implementation(projects.feature.auth.presentation)

            implementation(projects.feature.chat.data)
            implementation(projects.feature.chat.database)
            implementation(projects.feature.chat.domain)
            implementation(projects.feature.chat.presentation)

            implementation(libs.jetbrains.compose.foundation)
            implementation(libs.jetbrains.compose.material3)
            implementation(libs.jetbrains.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.jetbrains.compose.viewmodel)
            implementation(libs.jetbrains.compose.runtime)
            implementation(libs.jetbrains.lifecycle.compose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}