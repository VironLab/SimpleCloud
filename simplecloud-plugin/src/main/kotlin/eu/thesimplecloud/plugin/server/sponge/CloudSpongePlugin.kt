package eu.thesimplecloud.plugin.server.sponge

import com.google.inject.Inject
import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.player.ICloudPlayerManager
import eu.thesimplecloud.plugin.impl.player.CloudPlayerManagerSponge
import eu.thesimplecloud.plugin.listener.CloudListener
import eu.thesimplecloud.plugin.server.ICloudServerPlugin
import eu.thesimplecloud.plugin.server.sponge.listener.SpongeListener
import eu.thesimplecloud.plugin.startup.CloudPlugin
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.slf4j.Logger
import org.spongepowered.api.Game
import org.spongepowered.api.Server
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent
import org.spongepowered.api.event.lifecycle.StoppingEngineEvent
import org.spongepowered.plugin.PluginContainer
import org.spongepowered.plugin.jvm.Plugin
import kotlin.reflect.KClass

@Plugin("simplecloud_plugin")
class CloudSpongePlugin @Inject constructor(val pluginContainer: PluginContainer, val logger: Logger) :
    ICloudServerPlugin {

    lateinit var game: Game

    init {
        instance = this
        logger.info("______________________________________________________")
        logger.info("    Initialized SimpleCloud-Plugin on SpongeServer   ")
        logger.info("______________________________________________________")
    }

    companion object {
        @JvmStatic
        lateinit var instance: CloudSpongePlugin
    }

    @Listener
    fun init(event: ConstructPluginEvent) {
        this.game = event.game()
        CloudPlugin.instance.onEnable()
        CloudAPI.instance.getEventManager().registerListener(CloudPlugin.instance, CloudListener())
        game.eventManager().registerListeners(pluginContainer, SpongeListener())
        synchronizeOnlineCountTask()
    }

    override fun onBeforeFirstUpdate() {
        CloudPlugin.instance.thisService()
            .setMOTD(PlainTextComponentSerializer.plainText().serialize(game.server().motd()))
    }

    private fun synchronizeOnlineCountTask() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            val service = CloudPlugin.instance.thisService()
            if (service.getOnlineCount() != game.server().onlinePlayers().size) {
                service.setOnlineCount(game.server().onlinePlayers().size)
                service.update()
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    @Listener
    fun stop(event: StoppingEngineEvent<Server>) {
        CloudPlugin.instance.onDisable()
    }

    override fun getCloudPlayerManagerClass(): KClass<out ICloudPlayerManager> = CloudPlayerManagerSponge::class

    override fun shutdown() {
        game.server().shutdown()
    }
}