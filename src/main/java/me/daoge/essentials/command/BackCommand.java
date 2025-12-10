package me.daoge.essentials.command;

import org.allaymc.api.command.Command;
import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandNode;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.math.location.Location3dc;
import org.allaymc.api.permission.OpPermissionCalculator;
import org.allaymc.api.utils.TextFormat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Back command - teleports player to their last death location
 *
 * @author daoge
 */
public class BackCommand extends Command {

    // Store last death locations: playerUUID -> deathLocation
    private static final Map<UUID, Location3dc> deathLocations = new HashMap<>();

    public BackCommand() {
        super("back", "Return to your last death location", "essentials.command.back");
        OpPermissionCalculator.NON_OP_PERMISSIONS.addAll(this.permissions);
    }

    /**
     * Store a death location for a player
     */
    public static void setDeathLocation(UUID playerUUID, Location3dc location) {
        deathLocations.put(playerUUID, location);
    }

    /**
     * Remove a player's death location
     */
    public static void removeDeathLocation(UUID playerUUID) {
        deathLocations.remove(playerUUID);
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        CommandNode root = tree.getRoot();

        root.exec((context, player) -> {
            UUID playerUUID = player.getUniqueId();
            Location3dc deathLoc = deathLocations.get(playerUUID);

            if (deathLoc == null) {
                context.addError("You don't have a death location to return to!");
                return context.fail();
            }

            // Check if the dimension is still valid
            if (deathLoc.dimension() == null) {
                context.addError("The dimension of your death location is no longer available!");
                return context.fail();
            }

            // Teleport player to death location
            boolean success = player.teleport(deathLoc);
            if (success) {
                context.addOutput(TextFormat.GREEN + "Teleported to your last death location!");
            } else {
                context.addError("Teleportation failed!");
                return context.fail();
            }

            return context.success();
        }, SenderType.PLAYER);
    }
}
