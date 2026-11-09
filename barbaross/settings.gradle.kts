pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "barbaross"

include(":ipc")      // общий IPC-контракт (AIDL) между IDE и Build Runtime
include(":ide")      // приложение Barbaross IDE
include(":runtime")  // приложение Barbaross Build Runtime
