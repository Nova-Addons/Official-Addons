import xyz.xenondevs.origami.extension.OrigamiExtension

plugins {
    id("xyz.xenondevs.nova.nova-gradle-plugin")
}

repositories {
    mavenLocal { content { includeGroupAndSubgroups("xyz.xenondevs") } }
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.xenondevs.xyz/releases")
}

addon {
    addAddonJarToServerPlugins = false
}

extensions.configure<OrigamiExtension> {
    runServer.plugins.from(
        rootProject.subprojects
            .filter { it != project }
            .map { addonProject ->
                addonProject.tasks
                    .withType<Jar>()
                    .matching { it.name == "addonJar" }
            }
    )
    runServer.workingDirectory.set(layout.dir(providers.gradleProperty("serverDir").map(::File)))
}
