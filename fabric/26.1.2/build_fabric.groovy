dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    implementation "net.fabricmc:fabric-loader:${project.loader_version}"

    // Add Fabric API dependency
    modImplementation include(dependencies.find { it.name == 'fabric-api' })
}
