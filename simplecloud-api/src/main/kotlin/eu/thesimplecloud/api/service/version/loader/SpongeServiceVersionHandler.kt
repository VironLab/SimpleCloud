package eu.thesimplecloud.api.service.version.loader

import eu.thesimplecloud.api.service.version.ServiceVersion
import eu.thesimplecloud.api.service.version.type.ServiceAPIType

class SpongeServiceVersionHandler : IServiceVersionLoader {
    override fun loadVersions(): List<ServiceVersion> = listOf(
        ServiceVersion(
            "SPONGE_API_8",
            ServiceAPIType.SPONGE,
            "https://repo.spongepowered.org/repository/maven-releases/org/spongepowered/spongevanilla/1.16.5-8.0.0-RC823/spongevanilla-1.16.5-8.0.0-RC823-universal.jar"
        )
    )
}