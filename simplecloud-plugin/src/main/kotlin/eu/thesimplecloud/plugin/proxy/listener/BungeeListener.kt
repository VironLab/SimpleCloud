package eu.thesimplecloud.plugin.proxy.listener

import eu.thesimplecloud.api.CloudAPI
import eu.thesimplecloud.api.network.packets.player.PacketIORemoveCloudPlayer
import eu.thesimplecloud.api.network.packets.player.PacketIOUpdateCloudPlayer
import eu.thesimplecloud.api.player.CloudPlayer
import eu.thesimplecloud.api.player.connection.DefaultPlayerAddress
import eu.thesimplecloud.api.player.connection.DefaultPlayerConnection
import eu.thesimplecloud.api.player.text.CloudText
import eu.thesimplecloud.plugin.network.packets.PacketOutCreateCloudPlayer
import eu.thesimplecloud.plugin.network.packets.PacketOutPlayerLoginRequest
import eu.thesimplecloud.plugin.proxy.text.CloudTextBuilder
import eu.thesimplecloud.plugin.startup.CloudPlugin
import net.md_5.bungee.api.event.*
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.md_5.bungee.event.EventPriority
import org.bukkit.Bukkit

class BungeeListener : Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    fun on(event: LoginEvent) {
        val connection = event.connection
        val playerAddress = DefaultPlayerAddress(connection.address.hostName, connection.address.port)
        println(connection.uniqueId)
        val playerConnection = DefaultPlayerConnection(playerAddress, connection.name, connection.uniqueId, connection.isOnlineMode, connection.version)

        //send login request
        CloudPlugin.instance.communicationClient.sendUnitQuery(PacketOutCreateCloudPlayer(playerConnection, CloudPlugin.instance.thisServiceName)).awaitUninterruptibly()
    }

    @EventHandler
    fun on(event: PostLoginEvent) {
        val proxiedPlayer = event.player
        proxiedPlayer.reconnectServer = null
        CloudPlugin.instance.communicationClient.sendUnitQuery(PacketOutPlayerLoginRequest(proxiedPlayer.uniqueId)).addFailureListener {
            proxiedPlayer.disconnect(CloudTextBuilder().build(CloudText("§cLogin failed: " + it.message)))
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun on(event: PlayerDisconnectEvent) {
        println(event.player.uniqueId)
        val cloudPlayer = CloudAPI.instance.getCloudPlayerManager().getCachedCloudPlayer(event.player.uniqueId)
        cloudPlayer?.let {
            cloudPlayer as CloudPlayer
            cloudPlayer.setOffline()
            cloudPlayer.let { CloudAPI.instance.getCloudPlayerManager().removeCloudPlayer(it) }
            //send update that the player is now offline
            CloudPlugin.instance.communicationClient.sendUnitQuery(PacketIOUpdateCloudPlayer(cloudPlayer)).addCompleteListener {
                CloudPlugin.instance.communicationClient.sendUnitQuery(PacketIORemoveCloudPlayer(cloudPlayer.getUniqueId()))
            }
        }

    }



}