package eu.thesimplecloud.plugin.impl.player

import eu.thesimplecloud.api.exception.NoSuchPlayerException
import eu.thesimplecloud.api.exception.NoSuchServiceException
import eu.thesimplecloud.api.exception.NoSuchWorldException
import eu.thesimplecloud.api.location.ServiceLocation
import eu.thesimplecloud.api.location.SimpleLocation
import eu.thesimplecloud.api.network.packets.player.*
import eu.thesimplecloud.api.player.ICloudPlayer
import eu.thesimplecloud.api.player.connection.ConnectionResponse
import eu.thesimplecloud.api.player.text.CloudText
import eu.thesimplecloud.api.service.ICloudService
import eu.thesimplecloud.clientserverapi.lib.packet.packetsender.sendQuery
import eu.thesimplecloud.clientserverapi.lib.promise.CommunicationPromise
import eu.thesimplecloud.clientserverapi.lib.promise.ICommunicationPromise
import eu.thesimplecloud.plugin.network.packets.PacketOutTeleportOtherService
import eu.thesimplecloud.plugin.server.sponge.CloudSpongePlugin
import eu.thesimplecloud.plugin.startup.CloudPlugin
import org.spongepowered.api.ResourceKey
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.server.ServerLocation
import org.spongepowered.api.world.server.ServerWorld
import org.spongepowered.math.vector.Vector3d

class CloudPlayerManagerSponge : AbstractServiceCloudPlayerManager() {
    override fun sendMessageToPlayer(cloudPlayer: ICloudPlayer, cloudText: CloudText): ICommunicationPromise<Unit> =
        CloudPlugin.instance.connectionToManager.sendUnitQuery(
            PacketIOSendMessageToCloudPlayer(cloudPlayer, cloudText)
        )

    override fun connectPlayer(
        cloudPlayer: ICloudPlayer,
        cloudService: ICloudService
    ): ICommunicationPromise<ConnectionResponse> = CloudPlugin.instance.connectionToManager.sendQuery(
        PacketIOConnectCloudPlayer(cloudPlayer, cloudService), 500
    )

    override fun kickPlayer(cloudPlayer: ICloudPlayer, message: String): ICommunicationPromise<Unit> =
        CloudPlugin.instance.connectionToManager.sendUnitQuery(
            PacketIOKickCloudPlayer(cloudPlayer, message)
        )

    override fun sendTitle(
        cloudPlayer: ICloudPlayer,
        title: String,
        subTitle: String,
        fadeIn: Int,
        stay: Int,
        fadeOut: Int
    ) {
        CloudPlugin.instance.connectionToManager.sendUnitQuery(
            PacketIOSendTitleToCloudPlayer(
                cloudPlayer,
                title,
                subTitle,
                fadeIn,
                stay,
                fadeOut
            )
        )
    }

    override fun forcePlayerCommandExecution(cloudPlayer: ICloudPlayer, command: String) {
        CloudPlugin.instance.connectionToManager.sendUnitQuery(
            PacketIOCloudPlayerForceCommandExecution(
                cloudPlayer,
                command
            )
        )
    }

    override fun sendActionbar(cloudPlayer: ICloudPlayer, actionbar: String) {
        CloudPlugin.instance.connectionToManager.sendUnitQuery(
            PacketIOSendActionbarToCloudPlayer(
                cloudPlayer,
                actionbar
            )
        )
    }

    override fun sendTablist(cloudPlayer: ICloudPlayer, headers: Array<String>, footers: Array<String>) {
        CloudPlugin.instance.connectionToManager.sendUnitQuery(
            PacketIOSendTablistToPlayer(
                cloudPlayer.getUniqueId(),
                headers,
                footers
            )
        )
    }

    override fun teleportPlayer(cloudPlayer: ICloudPlayer, location: SimpleLocation): ICommunicationPromise<Unit> {
        if (CloudPlugin.instance.thisServiceName != cloudPlayer.getConnectedServerName()) {
            return CloudPlugin.instance.connectionToManager.sendUnitQuery(PacketIOTeleportPlayer(cloudPlayer, location))
        }

        val spongePlayer = getPlayerByCloudPlayer(cloudPlayer) ?: return CommunicationPromise.failed(
            NoSuchPlayerException("Unable to find the player on the server service")
        )
        val location = getLocationBySimpleLocation(location) ?: return CommunicationPromise.failed(NoSuchWorldException("Unable to find world: ${location.worldName}"))
        spongePlayer.setLocationAndRotation(location.first, location.second)
        return CommunicationPromise.of(Unit)
    }

    override fun teleportPlayer(cloudPlayer: ICloudPlayer, location: ServiceLocation): ICommunicationPromise<Unit> {
        if (location.getService() == null) return CommunicationPromise.failed(NoSuchServiceException("Service to connect the player to cannot be found."))
        return CloudPlugin.instance.connectionToManager.sendUnitQuery(
            PacketOutTeleportOtherService(cloudPlayer.getUniqueId(), location.serviceName, location as SimpleLocation),
            1000
        )
    }

    override fun hasPermission(cloudPlayer: ICloudPlayer, permission: String): ICommunicationPromise<Boolean> = CloudPlugin.instance.connectionToManager.sendQuery(PacketIOPlayerHasPermission(cloudPlayer.getUniqueId(), permission), 400)

    override fun getLocationOfPlayer(cloudPlayer: ICloudPlayer): ICommunicationPromise<ServiceLocation> {
        if (CloudPlugin.instance.thisServiceName != cloudPlayer.getConnectedServerName()) {
            return CloudPlugin.instance.connectionToManager.sendQuery(PacketIOGetPlayerLocation(cloudPlayer))
        }

        val player = getPlayerByCloudPlayer(cloudPlayer) ?: return CommunicationPromise.failed(NoSuchPlayerException("Unable to find bukkit player"))
        val playerLocation = player.serverLocation()
        playerLocation.world()
            ?: return CommunicationPromise.failed(NoSuchWorldException("The world the player is on is null"))
        val lookAt = player.headRotation().get()
        return CommunicationPromise.of(ServiceLocation(CloudPlugin.instance.thisService(), playerLocation.worldKey().namespace(), playerLocation.x(), playerLocation.y(), playerLocation.z(), lookAt.x().toFloat(), lookAt.y().toFloat()))
    }

    override fun sendPlayerToLobby(cloudPlayer: ICloudPlayer): ICommunicationPromise<Unit> = CloudPlugin.instance.connectionToManager.sendQuery(PacketIOSendPlayerToLobby(cloudPlayer.getUniqueId()))

    private fun getLocationBySimpleLocation(simpleLocation: SimpleLocation): Pair<ServerLocation, Vector3d>? {
        val world = Sponge.server().worldManager().world(ResourceKey.minecraft(simpleLocation.worldName)).orElseGet { null } ?: return null
        return Pair(ServerLocation.of(world, Vector3d(simpleLocation.x, simpleLocation.y, simpleLocation.z)), Vector3d(simpleLocation.yaw, simpleLocation.pitch, 0F))
    }

    private fun getPlayerByCloudPlayer(cloudPlayer: ICloudPlayer): Player? =
        Sponge.server().player(cloudPlayer.getUniqueId()).orElseGet { null }


}