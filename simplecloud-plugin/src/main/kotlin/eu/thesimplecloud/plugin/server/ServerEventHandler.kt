package eu.thesimplecloud.plugin.server;

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.plugin.startup.CloudPlugin
import java.util.*

object ServerEventHandler {

    private val UNKNOWN_ADRESS = "§cYou are connected from an unknown address!"
    private val NOT_REGISTERED = "§cYou are not registered on the network!"

    fun updateCurrentOnlineCountTo(count: Int) {
        val thisService = CloudPlugin.instance.thisService()
        thisService.setOnlineCount(count)
        thisService.update()
    }

    fun handleDisconnected(player: UUID, newPlayerCount: Int) {
        val playerManager = CloudAPI.instance.getCloudPlayerManager()
        val cloudPlayer = playerManager.getCachedCloudPlayer(player)

        if (cloudPlayer != null && !cloudPlayer.isUpdatesEnabled()) {
            playerManager.delete(cloudPlayer)
        }
        updateCurrentOnlineCountTo(newPlayerCount)
    }

    fun handleLogin(uuid: UUID, hostAddress: String): String? {
        if (hostAddress != "127.0.0.1" && !CloudAPI.instance.getWrapperManager().getAllCachedObjects()
                .any { it.getHost() == hostAddress }
        ) {
            return UNKNOWN_ADRESS
        }

        if (CloudAPI.instance.getCloudPlayerManager().getCachedCloudPlayer(uuid) == null) {
            return NOT_REGISTERED
        }
        return null
    }

}