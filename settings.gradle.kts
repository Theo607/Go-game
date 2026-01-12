rootProject.name = "Go-game"
include("core","server","client")
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention").version("1.0.0")
}
