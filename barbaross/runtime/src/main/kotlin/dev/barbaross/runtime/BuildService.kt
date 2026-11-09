package dev.barbaross.runtime

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import dev.barbaross.ipc.BuildProtocol
import dev.barbaross.ipc.IBuildService

/** Сервис Build Runtime. Доступ защищён signature-permission (см. манифест). */
class BuildService : Service() {

    private val binder = object : IBuildService.Stub() {
        override fun getProtocolVersion(): Int = BuildProtocol.VERSION

        override fun ping(payload: String?): String = payload.orEmpty()

        override fun getToolchainStatus(): Bundle = ToolchainInspector.inspect(filesDir)
    }

    override fun onBind(intent: Intent?): IBinder = binder
}
