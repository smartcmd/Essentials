package me.daoge.essentials.command;

import me.daoge.essentials.HubManager;
import me.daoge.essentials.LocationRecord;
import org.allaymc.api.command.Command;
import org.allaymc.api.command.CommandResult;
import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandContext;
import org.allaymc.api.command.tree.CommandNode;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.math.location.Location3d;
import org.allaymc.api.permission.OpPermissionCalculator;
import org.allaymc.api.utils.TextFormat;

public class HubCommand extends Command {
    private final HubManager hubManager;

    public HubCommand(HubManager hubManager) {
        super("hub", "Teleport to the hub spawn location", "essentials.command.hub");
        OpPermissionCalculator.NON_OP_PERMISSIONS.addAll(this.permissions);
        this.hubManager = hubManager;
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        CommandNode root = tree.getRoot();
        root.exec((context, player) -> {
            if (!hubManager.hasHub()) {
                player.sendMessage(TextFormat.RED + "Hub location has not been set yet.");
                player.sendMessage(TextFormat.GRAY + "An administrator needs to use /sethub to set the hub location.");
                return context.fail();
            }

            LocationRecord hubLocation = hubManager.getHub();
            Location3d location = hubLocation.toLocation();

            if (location == null) {
                player.sendMessage(TextFormat.RED + "Failed to load hub location. The world may not exist.");
                return context.fail();
            }

            boolean success = player.teleport(location);
            if (success) {
                player.sendMessage(TextFormat.GREEN + "Teleported to hub!");
            } else {
                player.sendMessage(TextFormat.RED + "Failed to teleport to hub.");
            }

            return success ? context.success() : context.fail();
        }, SenderType.PLAYER);
    }
}
