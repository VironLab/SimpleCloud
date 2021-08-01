package eu.thesimplecloud.base.wrapper.process.serviceconfigurator.configurators

import eu.thesimplecloud.api.service.ICloudService
import eu.thesimplecloud.api.utils.ConfigurationFileEditor
import eu.thesimplecloud.base.wrapper.process.serviceconfigurator.IServiceConfigurator
import java.io.File
import java.io.FileWriter

class DefaultSpongeServiceConfigurator : IServiceConfigurator {

    val defaultPropsFileContent = """#Minecraft server properties
enable-jmx-monitoring=false
rcon.port=25575
level-seed=
gamemode=survival
enable-command-block=false
enable-query=false
generator-settings=
level-name=world
motd=A Minecraft Server
query.port=25565
pvp=true
generate-structures=true
difficulty=easy
network-compression-threshold=256
max-tick-time=60000
max-players=20
use-native-transport=true
online-mode=false
enable-status=true
allow-flight=false
broadcast-rcon-to-ops=true
view-distance=10
max-build-height=256
server-ip=
allow-nether=true
server-port=25565
enable-rcon=false
sync-chunk-writes=true
op-permission-level=4
prevent-proxy-connections=false
resource-pack=
entity-broadcast-range-percentage=100
rcon.password=
player-idle-timeout=0
force-gamemode=false
rate-limit=0
hardcore=false
white-list=false
broadcast-console-to-ops=true
spawn-npcs=true
spawn-animals=true
snooper-enabled=true
function-permission-level=2
level-type=default
text-filtering-config=
spawn-monsters=true
enforce-whitelist=false
resource-pack-sha1=
spawn-protection=16
max-world-size=29999984
"""

    override fun configureService(cloudService: ICloudService, serviceTmpDirectory: File) {
        val propertiesFile = File(serviceTmpDirectory, "server.properties").also { f ->
            if (!f.exists()) {
                f.createNewFile(); FileWriter(f).also { writer ->
                    writer.write(defaultPropsFileContent)
                    writer.flush()
                    writer.close()
                }
            }
        }
        val eulaFile = File(serviceTmpDirectory, "eula.txt").also { f ->
            if (!f.exists()) {
                f.createNewFile(); FileWriter(f).also { writer ->
                    writer.write("eula=true")
                    writer.flush()
                    writer.close()
                }
            }
        }
        val eulaEditor = ConfigurationFileEditor(eulaFile, ConfigurationFileEditor.PROPERTIES_SPLITTER)
        eulaEditor.setValue("eula", "true")
        eulaEditor.saveToFile(eulaFile)
        val fileEditor = ConfigurationFileEditor(propertiesFile, ConfigurationFileEditor.PROPERTIES_SPLITTER)
        fileEditor.setValue("server-ip", cloudService.getHost())
        fileEditor.setValue("server-port", cloudService.getPort().toString())
        fileEditor.setValue("max-players", cloudService.getMaxPlayers().toString())
        fileEditor.saveToFile(propertiesFile)
    }
}