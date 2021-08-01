package eu.thesimplecloud.plugin.server.sponge.listener

import eu.thesimplecloud.plugin.server.ServerEventHandler
import net.kyori.adventure.text.Component
import org.spongepowered.api.Sponge
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.network.ServerSideConnectionEvent


class SpongeListener {

    @Listener
    fun handleLogin(event: ServerSideConnectionEvent.Login) {
        val msg =
            ServerEventHandler.handleLogin(event.user().uniqueId(), event.connection().address().address.hostAddress)
        if (msg != null) {
            event.isCancelled = true
            event.setMessage(Component.text(msg))
        }
    }

    @Listener
    fun handleDisconnect(event: ServerSideConnectionEvent.Disconnect) =
        ServerEventHandler.handleDisconnected(event.player().uniqueId(), Sponge.server().onlinePlayers().size - 1)

}