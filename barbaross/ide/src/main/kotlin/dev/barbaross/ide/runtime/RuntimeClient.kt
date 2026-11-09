package dev.barbaross.ide.runtime

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import dev.barbaross.ipc.BuildProtocol
import dev.barbaross.ipc.IBuildService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Состояние связи IDE с Build Runtime. */
sealed interface RuntimeState {
    data object Disconnected : RuntimeState
    data object NotInstalled : RuntimeState
    data class PermissionDenied(val detail: String) : RuntimeState
    data class IncompatibleProtocol(val runtimeVersion: Int) : RuntimeState
    data class Connected(
        val protocolVersion: Int,
        val jdk: Boolean,
        val gradle: Boolean,
        val sdk: Boolean,
    ) : RuntimeState
    data class Error(val detail: String) : RuntimeState
}

/** Реальный bind к Build Runtime. Вызовы Binder выполняются не на главном потоке вызывающей стороной. */
class RuntimeClient(private val context: Context) {
    private val _state = MutableStateFlow<RuntimeState>(RuntimeState.Disconnected)
    val state: StateFlow<RuntimeState> = _state.asStateFlow()

    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            _state.value = try {
                val api = IBuildService.Stub.asInterface(service)
                    ?: throw IllegalStateException("Runtime вернул пустой Binder")
                val version = api.getProtocolVersion()
                if (!BuildProtocol.isCompatible(version)) {
                    RuntimeState.IncompatibleProtocol(version)
                } else {
                    val s = api.getToolchainStatus()
                    RuntimeState.Connected(
                        protocolVersion = version,
                        jdk = s.getBoolean(BuildProtocol.KEY_JDK_INSTALLED),
                        gradle = s.getBoolean(BuildProtocol.KEY_GRADLE_INSTALLED),
                        sdk = s.getBoolean(BuildProtocol.KEY_SDK_INSTALLED),
                    )
                }
            } catch (e: Exception) {
                RuntimeState.Error(e.message ?: e.javaClass.simpleName)
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            _state.value = RuntimeState.Disconnected
        }
    }

    fun connect() {
        if (bound) return
        val intent = Intent(BuildProtocol.ACTION_BIND).setPackage(BuildProtocol.RUNTIME_PACKAGE)
        try {
            bound = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            if (!bound) {
                // Сервис не найден либо разрешение не выдано системой.
                _state.value = RuntimeState.NotInstalled
                context.unbindService(connection)
            }
        } catch (e: SecurityException) {
            bound = false
            _state.value = RuntimeState.PermissionDenied(
                "Runtime подписан другим ключом или permission не выдано: ${e.message}"
            )
        }
    }

    fun disconnect() {
        if (bound) {
            context.unbindService(connection)
            bound = false
        }
        _state.value = RuntimeState.Disconnected
    }
}
