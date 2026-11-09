package dev.barbaross.runtime

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import dev.barbaross.ipc.BuildProtocol

/** Простой экран состояния Runtime: показывает реальное состояние инструментов. */
class RuntimeStatusActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val status = ToolchainInspector.inspect(filesDir)
        val text = buildString {
            appendLine("Barbaross Build Runtime")
            appendLine("Протокол IPC: v${BuildProtocol.VERSION}")
            appendLine()
            appendLine("JDK: ${yesNo(status.getBoolean(BuildProtocol.KEY_JDK_INSTALLED))}")
            appendLine("Gradle: ${yesNo(status.getBoolean(BuildProtocol.KEY_GRADLE_INSTALLED))}")
            appendLine("Android SDK: ${yesNo(status.getBoolean(BuildProtocol.KEY_SDK_INSTALLED))}")
            appendLine()
            appendLine("Сборка APK пока недоступна: инструменты не установлены.")
            appendLine("Установка инструментов появится на этапе 5 (см. docs/PLAN.md).")
        }
        setContentView(TextView(this).apply {
            this.text = text
            setPadding(48, 96, 48, 48)
            textSize = 16f
        })
    }

    private fun yesNo(v: Boolean) = if (v) "установлен" else "не установлен"
}
