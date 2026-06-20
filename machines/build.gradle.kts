plugins {
    id("addons.common-conventions")
}

version = "0.10.0-RC.1"

dependencies {
    implementation(project(":simple-upgrades"))
}

addon {
    name = "Machines"
    main = "xyz.xenondevs.nova.addon.machines.Machines"
    version = project.version.toString()
    authors = listOf("StudioCode", "ByteZ", "Javahase")
    dependency("Simple_Upgrades")
}

pluginPublish {
    hangar("Machines") {
        gameVersions(libs.versions.minecraft.get())
        requiredDependency("Nova")
        requiredDependency("Simple-Upgrades")
    }
    modrinth("rFmCpMcN") {
        gameVersions(libs.versions.minecraft.get())
        requiredDependency("yCVqpwUy") // Nova
        requiredDependency("uUuVhtJ7") // Simple-Upgrades
    }
}