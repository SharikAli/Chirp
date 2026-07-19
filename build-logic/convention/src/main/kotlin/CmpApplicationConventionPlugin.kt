import com.chatapp.chirp.convention.applyHierarchyTemplate
import com.chatapp.chirp.convention.configureAndroidLibraryTarget
import com.chatapp.chirp.convention.configureDesktopTarget
import com.chatapp.chirp.convention.configureIosTargets
import com.chatapp.chirp.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class CmpApplicationConventionPlugin: Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.kotlin.multiplatform.library")
                apply("org.jetbrains.kotlin.multiplatform")
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            configureAndroidLibraryTarget()
            configureIosTargets()
            configureDesktopTarget()

            extensions.configure<KotlinMultiplatformExtension> {
                applyHierarchyTemplate()
            }

            dependencies {
                "androidMainImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
            }
        }
    }
}