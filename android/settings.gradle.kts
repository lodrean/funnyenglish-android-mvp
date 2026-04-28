pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://artifactory-external.vkpartner.ru/artifactory/maven/")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://artifactory-external.vkpartner.ru/artifactory/maven/")
        }
    }
}

rootProject.name = "FunnyEnglish"
include(":app")
include(":core:design-system")
include(":core:domain")
include(":core:data")
include(":core:presentation")
include(":feature:home")
include(":feature:dictionary")
include(":feature:quiz")
include(":feature:chat")
include(":feature:games")
include(":feature:profile")
include(":model_asset_pack")
