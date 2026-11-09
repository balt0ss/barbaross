package dev.barbaross.runtime

import android.os.Bundle
import dev.barbaross.ipc.BuildProtocol
import java.io.File

/**
 * Реально проверяет наличие инструментов в приватном каталоге Runtime
 * (filesDir/toolchain/{jdk,gradle,sdk}). Установка инструментов — этап 5.
 */
object ToolchainInspector {
    fun inspect(filesDir: File): Bundle {
        val root = File(filesDir, "toolchain")
        val jdk = File(root, "jdk/bin/java").isFile
        val gradle = File(root, "gradle/bin/gradle").isFile
        val sdk = File(root, "sdk/platforms").isDirectory

        return Bundle().apply {
            putBoolean(BuildProtocol.KEY_JDK_INSTALLED, jdk)
            putBoolean(BuildProtocol.KEY_GRADLE_INSTALLED, gradle)
            putBoolean(BuildProtocol.KEY_SDK_INSTALLED, sdk)
            putString(BuildProtocol.KEY_DETAILS, "toolchain root: ${root.absolutePath}")
        }
    }
}
