pluginManagement {
    repositories {
        mavenLocal()
//        maven(url = "https://repo.binom.pw")
        mavenCentral()
        gradlePluginPortal()
        google()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
//        maven(url = "https://repo.binom.pw")
        google()
        mavenCentral()
    }
}
rootProject.name = "kDNS"
include(":protocol")
//include(":ktor")
