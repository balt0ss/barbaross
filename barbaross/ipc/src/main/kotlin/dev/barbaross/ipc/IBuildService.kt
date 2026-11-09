package dev.barbaross.ipc

import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel

/**
 * Контракт между Barbaross IDE и Barbaross Build Runtime.
 *
 * Написан вручную (без AIDL), потому что инструмент `aidl` из Android Build Tools
 * существует только под x86_64 и не запускается на arm64-телефоне, где идёт сборка.
 * Wire-формат стандартный для Binder: проверка interface token, затем аргументы.
 *
 * Версионирование: BuildProtocol.VERSION. Новые методы — только в конец, с новым кодом транзакции.
 */
interface IBuildService : IInterface {
    /** Версия протокола, которую реализует Runtime. */
    fun getProtocolVersion(): Int

    /** Проверка связи: возвращает переданную строку. */
    fun ping(payload: String?): String

    /** Состояние инструментов (ключи BuildProtocol.KEY_*). */
    fun getToolchainStatus(): Bundle

    /** Серверная сторона: наследуется в Runtime. */
    abstract class Stub : Binder(), IBuildService {
        init {
            attachInterface(this, DESCRIPTOR)
        }

        override fun asBinder(): IBinder = this

        override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            when (code) {
                IBinder.INTERFACE_TRANSACTION -> {
                    reply?.writeString(DESCRIPTOR)
                    return true
                }
                TX_GET_PROTOCOL_VERSION -> {
                    data.enforceInterface(DESCRIPTOR)
                    val result = getProtocolVersion()
                    reply?.writeNoException()
                    reply?.writeInt(result)
                    return true
                }
                TX_PING -> {
                    data.enforceInterface(DESCRIPTOR)
                    val result = ping(data.readString())
                    reply?.writeNoException()
                    reply?.writeString(result)
                    return true
                }
                TX_GET_TOOLCHAIN_STATUS -> {
                    data.enforceInterface(DESCRIPTOR)
                    val result = getToolchainStatus()
                    reply?.writeNoException()
                    reply?.writeBundle(result)
                    return true
                }
            }
            return super.onTransact(code, data, reply, flags)
        }

        companion object {
            const val DESCRIPTOR = "dev.barbaross.ipc.IBuildService"
            const val TX_GET_PROTOCOL_VERSION = IBinder.FIRST_CALL_TRANSACTION
            const val TX_PING = IBinder.FIRST_CALL_TRANSACTION + 1
            const val TX_GET_TOOLCHAIN_STATUS = IBinder.FIRST_CALL_TRANSACTION + 2

            /** Клиентская сторона: оборачивает IBinder в интерфейс. */
            fun asInterface(binder: IBinder?): IBuildService? {
                if (binder == null) return null
                val local = binder.queryLocalInterface(DESCRIPTOR)
                if (local is IBuildService) return local
                return IBuildServiceProxy(binder)
            }
        }
    }
}

internal class IBuildServiceProxy(private val remote: IBinder) : IBuildService {
    override fun asBinder(): IBinder = remote

    override fun getProtocolVersion(): Int = call(IBuildService.Stub.TX_GET_PROTOCOL_VERSION, {}) { it.readInt() }

    override fun ping(payload: String?): String =
        call(IBuildService.Stub.TX_PING, { it.writeString(payload) }) { it.readString().orEmpty() }

    override fun getToolchainStatus(): Bundle =
        call(IBuildService.Stub.TX_GET_TOOLCHAIN_STATUS, {}) {
            it.readBundle(IBuildService::class.java.classLoader) ?: Bundle.EMPTY
        }

    private inline fun <T> call(code: Int, write: (Parcel) -> Unit, read: (Parcel) -> T): T {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken(IBuildService.Stub.DESCRIPTOR)
            write(data)
            remote.transact(code, data, reply, 0)
            reply.readException()
            return read(reply)
        } finally {
            reply.recycle()
            data.recycle()
        }
    }
}
