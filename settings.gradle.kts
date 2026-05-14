pluginManagement {
    repositories {
        google()        // Cruciaal voor Android plugins
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()        // Cruciaal voor Android bibliotheken
        mavenCentral()
    }
}

rootProject.name = "Appetijt"
include(":app")
include(":data")
include(":domain")
include(":ui")