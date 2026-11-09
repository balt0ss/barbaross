package dev.barbaross.ipc

/** Константы и правила совместимости протокола IDE <-> Build Runtime. */
object BuildProtocol {
    /** Текущая версия протокола. Увеличивается при любом несовместимом изменении AIDL. */
    const val VERSION = 1

    /** Минимальная версия, с которой эта сборка умеет работать. */
    const val MIN_SUPPORTED_VERSION = 1

    /** Пакет и action сервиса Runtime. */
    const val RUNTIME_PACKAGE = "dev.barbaross.runtime"
    const val ACTION_BIND = "dev.barbaross.runtime.action.BUILD_SERVICE"

    /** Signature-permission: выдаётся только приложениям, подписанным тем же ключом. */
    const val PERMISSION_BIND = "dev.barbaross.permission.BIND_BUILD_RUNTIME"

    const val KEY_JDK_INSTALLED = "jdk_installed"
    const val KEY_GRADLE_INSTALLED = "gradle_installed"
    const val KEY_SDK_INSTALLED = "sdk_installed"
    const val KEY_DETAILS = "details"

    const val STATUS_SUCCESS = 0
    const val STATUS_FAILED = 1
    const val STATUS_CANCELLED = 2

    /** Совместима ли версия протокола Runtime с этой сборкой IDE. */
    fun isCompatible(runtimeVersion: Int): Boolean =
        runtimeVersion in MIN_SUPPORTED_VERSION..VERSION
}
