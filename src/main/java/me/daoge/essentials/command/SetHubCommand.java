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
import org.allaymc.api.utils.TextFormat;

public class SetHubCommand extends Command {
    private final HubManager hubManager;

    public SetHubCommand(HubManager hubManager) {
        super("sethub", "Set the hub spawn location", "essentials.command.sethub");
        this.hubManager = hubManager;
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        CommandNode root = tree.getRoot();
        root.exec((context, player) -> {
            LocationRecord location = LocationRecord.from("hub", player.getLocation());
            hubManager.setHub(location);

            player.sendMessage(TextFormat.GREEN + "Hub location has been set to your current position.");
            player.sendMessage(TextFormat.GRAY + String.format("World: %s, X: %.2f, Y: %.2f, Z: %.2f",
                    location.worldName(), location.x(), location.y(), location.z()));

            return context.success();
        }, SenderType.PLAYER);
    }
}
