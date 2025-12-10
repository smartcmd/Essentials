package me.daoge.essentials;

import lombok.extern.slf4j.Slf4j;
import me.daoge.essentials.command.BackCommand;
import me.daoge.essentials.command.NoticeCommand;
import me.daoge.essentials.command.TpaCommand;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.eventbus.EventHandler;
import org.allaymc.api.eventbus.event.entity.EntityDieEvent;
import org.allaymc.api.eventbus.event.server.PlayerDisconnectEvent;
import org.allaymc.api.eventbus.event.server.PlayerJoinEvent;
import org.allaymc.api.math.location.Location3d;
import org.allaymc.api.player.Player;
import org.allaymc.api.utils.config.Config;
import org.allaymc.api.utils.config.ConfigSection;

import java.util.UUID;

/**
 * Listener for player death, disconnect and join events
 *
 * @author daoge
 */
@Slf4j
public class EventListener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if notice feature is enabled
        Config config = EssentialsPlugin.getInstance().getConfig();
        ConfigSection features = config.getSection("features");

        if (features.getBoolean("notice", true)) {
            // Show notice to player
            NoticeCommand.showNotice(player);
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        UUID playerUUID = event.getPlayer().getLoginData().getUuid();
        // Clean up TPA requests when player disconnects
        TpaCommand.removePendingRequest(playerUUID);
        // Clean up death location when player disconnects
        BackCommand.removeDeathLocation(playerUUID);
    }


    @EventHandler
    public void onEntityDie(EntityDieEvent event) {
        if (event.getEntity() instanceof EntityPlayer entityPlayer) {
            var deathLoc = new Location3d(entityPlayer.getLocation());
            BackCommand.setDeathLocation(entityPlayer.getUniqueId(), deathLoc);
            log.debug("Recorded death location for player: {}", entityPlayer.getDisplayName());
        }
    }
}
