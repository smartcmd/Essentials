package me.daoge.essentials;

import lombok.extern.slf4j.Slf4j;
import me.daoge.essentials.command.BackCommand;
import me.daoge.essentials.command.TpaCommand;
import org.allaymc.api.entity.Entity;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.eventbus.EventHandler;
import org.allaymc.api.eventbus.event.entity.EntityDieEvent;
import org.allaymc.api.eventbus.event.server.PlayerDisconnectEvent;
import org.allaymc.api.math.location.Location3d;
import org.allaymc.api.player.Player;

import java.util.UUID;

/**
 * Listener for player death and disconnect events
 * 
 * @author daoge
 */
@Slf4j
public class Listener {
    
    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            UUID playerUUID = player.getLoginData().getUuid();
            // Clean up TPA requests when player disconnects
            TpaCommand.removePendingRequest(playerUUID);
            // Clean up death location when player disconnects
            BackCommand.removeDeathLocation(playerUUID);
        }
    }
    

    @EventHandler
    public void onEntityDie(EntityDieEvent event) {
        Entity entity = event.getEntity();
        
        // Check if entity is a player
        if (entity instanceof EntityPlayer entityPlayer) {
            UUID playerUUID = entityPlayer.getUniqueId();
            
            // Get the death location
            if (entityPlayer.getLocation() != null) {
                var deathLoc = new Location3d(entityPlayer.getLocation());
                BackCommand.setDeathLocation(playerUUID, deathLoc);
                log.debug("Recorded death location for player: {}", entityPlayer.getDisplayName());
            }
        }
    }
}
