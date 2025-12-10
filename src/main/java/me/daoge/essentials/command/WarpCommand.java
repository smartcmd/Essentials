package me.daoge.essentials.command;

import me.daoge.essentials.WarpManager;
import me.daoge.essentials.LocationRecord;
import org.allaymc.api.command.Command;
import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandNode;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.form.Forms;
import org.allaymc.api.form.type.CustomForm;
import org.allaymc.api.form.type.SimpleForm;
import org.allaymc.api.math.location.Location3d;
import org.allaymc.api.permission.OpPermissionCalculator;
import org.allaymc.api.player.Player;
import org.allaymc.api.utils.TextFormat;

import java.util.List;
import java.util.Set;

/**
 * Warp command - manage and teleport to saved warp points.
 */
public class WarpCommand extends Command {

    private final WarpManager warpManager;

    public WarpCommand(WarpManager warpManager) {
        super("warp", "Teleport to or manage warp points", "essentials.command.warp");
        this.warpManager = warpManager;
        OpPermissionCalculator.NON_OP_PERMISSIONS.addAll(Set.of(
                "essentials.command.warp",
                "essentials.command.warp.tp",
                "essentials.command.warp.list"
        ));
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        CommandNode root = tree.getRoot();

        root.key("tp")
                .permission("essentials.command.warp.tp")
                .exec((context, entityPlayer) -> {
                    Player player = entityPlayer.getController();

                    List<LocationRecord> warps = warpManager.getSortedWarps();
                    if (warps.isEmpty()) {
                        context.addError("No warps available!");
                        return context.fail();
                    }

                    SimpleForm form = Forms.simple()
                            .title("Warp")
                            .content("Choose a warp to teleport to.");

                    warps.forEach(warp -> form.button(warp.name())
                            .onClick(button -> teleportPlayer(entityPlayer, player, warp)));

                    form.onClose(() -> player.sendMessage(TextFormat.YELLOW + "Warp selection closed."));
                    player.viewForm(form);

                    return context.success();
                }, SenderType.ACTUAL_PLAYER);

        root.key("list")
                .permission("essentials.command.warp.list")
                .exec((context, entityPlayer) -> {
                    List<LocationRecord> warps = warpManager.getSortedWarps();
                    if (warps.isEmpty()) {
                        context.addOutput(TextFormat.YELLOW + "No warps available.");
                        return context.success();
                    }
                    String names = String.join(", ", warps.stream().map(LocationRecord::name).toList());
                    context.addOutput(TextFormat.GREEN + "Warps: " + names);
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);

        root.key("add")
                .permission("essentials.command.warp.add")
                .exec((context, entityPlayer) -> {
                    Player player = entityPlayer.getController();

                    if (entityPlayer.getLocation() == null) {
                        context.addError("Cannot capture your current location!");
                        return context.fail();
                    }

                    Location3d snapshot = new Location3d(entityPlayer.getLocation());
                    showAddWarpForm(player, snapshot);
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);

        root.key("remove")
                .permission("essentials.command.warp.remove")
                .exec((context, entityPlayer) -> {
                    Player player = entityPlayer.getController();

                    List<LocationRecord> warps = warpManager.getSortedWarps();
                    if (warps.isEmpty()) {
                        context.addError("No warps available to remove!");
                        return context.fail();
                    }

                    SimpleForm form = Forms.simple()
                            .title("Remove Warp")
                            .content("Select a warp to delete.");

                    warps.forEach(warp -> form.button(warp.name()).onClick(button -> {
                        boolean removed = warpManager.removeWarp(warp.name());
                        if (removed) {
                            player.sendMessage(TextFormat.YELLOW + "Removed warp " + warp.name() + ".");
                        } else {
                            player.sendMessage(TextFormat.RED + "Warp " + warp.name() + " no longer exists.");
                        }
                    }));

                    form.onClose(() -> player.sendMessage(TextFormat.YELLOW + "Warp removal cancelled."));
                    player.viewForm(form);

                    return context.success();
                }, SenderType.ACTUAL_PLAYER);
    }

    private void showAddWarpForm(Player player, Location3d snapshot) {
        CustomForm form = Forms.custom()
                .title("Add Warp")
                .input("Warp name", "Enter a unique name");

        form.onResponse(responses -> {
            String name = responses.isEmpty() ? "" : responses.getFirst();
            name = name.trim();

            if (name.isEmpty()) {
                player.sendMessage(TextFormat.RED + "Warp name cannot be empty.");
                return;
            }

            boolean added = warpManager.addWarp(name, snapshot);
            if (added) {
                player.sendMessage(TextFormat.GREEN + "Warp " + name + " has been created.");
            } else if (warpManager.getWarp(name).isPresent()) {
                player.sendMessage(TextFormat.RED + "Warp \"" + name + "\" already exists.");
            } else {
                player.sendMessage(TextFormat.RED + "Failed to save warp location.");
            }
        }).onClose(() -> player.sendMessage(TextFormat.YELLOW + "Warp creation cancelled."));

        player.viewForm(form);
    }

    private void teleportPlayer(EntityPlayer entityPlayer, Player player, LocationRecord warp) {
        var location = warp.toLocation();
        if (location == null) {
            player.sendMessage(TextFormat.RED + "Warp location is unavailable (missing world or dimension).");
            return;
        }

        boolean success = entityPlayer.teleport(location);
        if (success) {
            player.sendMessage(TextFormat.GREEN + "Teleported to warp " + warp.name() + "!");
        } else {
            player.sendMessage(TextFormat.RED + "Teleportation failed.");
        }
    }

}
