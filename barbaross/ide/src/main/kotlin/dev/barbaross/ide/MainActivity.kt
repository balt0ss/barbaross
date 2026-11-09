package dev.barbaross.ide

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.barbaross.ide.runtime.RuntimeClient
import dev.barbaross.ide.runtime.RuntimeState

private enum class Section(val title: String, val stage: String?) {
    Projects("Проекты", "этап 2"),
    Editor("Редактор", "этапы 3–4"),
    Terminal("Терминал", "этап 5"),
    Extensions("Расширения", "этап 8"),
    Settings("Настройки", null),
}

class MainActivity : ComponentActivity() {
    private lateinit var runtime: RuntimeClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runtime = RuntimeClient(applicationContext)
        setContent {
            MaterialTheme {
                val state by runtime.state.collectAsState()
                var selected by rememberSaveable { mutableIntStateOf(Section.Settings.ordinal) }
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            Section.entries.forEach { s ->
                                NavigationBarItem(
                                    selected = selected == s.ordinal,
                                    onClick = { selected = s.ordinal },
                                    icon = {},
                                    label = { Text(s.title) },
                                )
                            }
                        }
                    },
                ) { padding ->
                    val section = Section.entries[selected]
                    Column(
                        Modifier.fillMaxSize().padding(padding).padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(section.title, style = MaterialTheme.typography.headlineSmall)
                        if (section == Section.Settings) {
                            RuntimeStatus(state, onRetry = { runtime.disconnect(); runtime.connect() })
                        } else {
                            Text("Раздел ещё не реализован. Планируется: ${section.stage}. См. docs/PLAN.md.")
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        runtime.connect()
    }

    override fun onStop() {
        runtime.disconnect()
        super.onStop()
    }
}

@Composable
private fun RuntimeStatus(state: RuntimeState, onRetry: () -> Unit) {
    Text("Build Runtime", style = MaterialTheme.typography.titleMedium)
    val text = when (state) {
        RuntimeState.Disconnected -> "Нет соединения."
        RuntimeState.NotInstalled ->
            "Barbaross Build Runtime не найден или недоступен. Установите его APK, подписанный тем же ключом."
        is RuntimeState.PermissionDenied -> "Доступ запрещён: ${state.detail}"
        is RuntimeState.IncompatibleProtocol ->
            "Версия протокола Runtime (${state.runtimeVersion}) несовместима с этой IDE. Обновите оба приложения."
        is RuntimeState.Connected ->
            "Подключено, протокол v${state.protocolVersion}.\n" +
                "JDK: ${if (state.jdk) "есть" else "нет"}; " +
                "Gradle: ${if (state.gradle) "есть" else "нет"}; " +
                "Android SDK: ${if (state.sdk) "есть" else "нет"}."
        is RuntimeState.Error -> "Ошибка связи: ${state.detail}"
    }
    Text(text)
    Button(onClick = onRetry) { Text("Проверить снова") }
}
