package me.daoge.essentials.command;

import me.daoge.essentials.HomeManager;
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
import org.allaymc.api.player.Player;
import org.allaymc.api.utils.TextFormat;

import java.util.List;
import java.util.UUID;

/**
 * Home command - manage and teleport to personal homes.
 */
public class HomeCommand extends Command {

    private final HomeManager homeManager;

    public HomeCommand(HomeManager homeManager) {
        super("home", "Teleport to or manage your homes", "essentials.command.home");
        this.homeManager = homeManager;
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        CommandNode root = tree.getRoot();

        root.key("tp")
                .permission("essentials.command.home.tp")
                .exec((context, entityPlayer) -> {
                    Player player = entityPlayer.getController();
                    UUID uuid = entityPlayer.getUniqueId();
                    List<LocationRecord> homes = homeManager.getSortedHomes(uuid);
                    if (homes.isEmpty()) {
                        context.addError("You have no homes set!");
                        return context.fail();
                    }

                    SimpleForm form = Forms.simple()
                            .title("Homes")
                            .content("Choose a home to teleport to.");

                    homes.forEach(home -> form.button(home.name())
                            .onClick(button -> teleportPlayer(entityPlayer, player, home)));

                    form.onClose(() -> player.sendMessage(TextFormat.YELLOW + "Home selection closed."));
                    player.viewForm(form);
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);

        root.key("add")
                .permission("essentials.command.home.add")
                .exec((context, entityPlayer) -> {
                    Player player = entityPlayer.getController();
                    if (entityPlayer.getLocation() == null) {
                        context.addError("Cannot capture your current location!");
                        return context.fail();
                    }

                    Location3d snapshot = new Location3d(entityPlayer.getLocation());
                    showAddHomeForm(player, entityPlayer.getUniqueId(), snapshot);
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);

        root.key("remove")
                .permission("essentials.command.home.remove")
                .exec((context, entityPlayer) -> {
                    Player player = entityPlayer.getController();
                    List<LocationRecord> homes = homeManager.getSortedHomes(entityPlayer.getUniqueId());
                    if (homes.isEmpty()) {
                        context.addError("You have no homes to remove!");
                        return context.fail();
                    }

                    SimpleForm form = Forms.simple()
                            .title("Remove Home")
                            .content("Select a home to delete.");

                    homes.forEach(home -> form.button(home.name()).onClick(button -> {
                        boolean removed = homeManager.removeHome(entityPlayer.getUniqueId(), home.name());
                        if (removed) {
                            player.sendMessage(TextFormat.YELLOW + "Removed home " + home.name() + ".");
                        } else {
                            player.sendMessage(TextFormat.RED + "Home " + home.name() + " no longer exists.");
                        }
                    }));

                    form.onClose(() -> player.sendMessage(TextFormat.YELLOW + "Home removal cancelled."));
                    player.viewForm(form);
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);

        root.key("list")
                .permission("essentials.command.home.list")
                .exec((context, entityPlayer) -> {
                    UUID uuid = entityPlayer.getUniqueId();
                    List<LocationRecord> homes = homeManager.getSortedHomes(uuid);
                    if (homes.isEmpty()) {
                        context.addOutput(TextFormat.YELLOW + "You have no homes.");
                        return context.success();
                    }
                    String names = String.join(", ", homes.stream().map(LocationRecord::name).toList());
                    context.addOutput(TextFormat.GREEN + "Homes: " + names);
                    return context.success();
                }, SenderType.ACTUAL_PLAYER);
    }

    private void showAddHomeForm(Player player, UUID uuid, Location3d snapshot) {
        CustomForm form = Forms.custom()
                .title("Add Home")
                .input("Home name", "Enter a unique name");

        form.onResponse(responses -> {
            String name = responses.isEmpty() ? "" : responses.getFirst();
            name = name.trim();

            if (name.isEmpty()) {
                player.sendMessage(TextFormat.RED + "Home name cannot be empty.");
                return;
            }

            boolean added = homeManager.addHome(uuid, name, snapshot);
            if (added) {
                player.sendMessage(TextFormat.GREEN + "Home " + name + " has been created.");
            } else if (homeManager.getHome(uuid, name).isPresent()) {
                player.sendMessage(TextFormat.RED + "Home \"" + name + "\" already exists.");
            } else {
                player.sendMessage(TextFormat.RED + "Failed to save home location.");
            }
        }).onClose(() -> player.sendMessage(TextFormat.YELLOW + "Home creation cancelled."));

        player.viewForm(form);
    }

    private void teleportPlayer(EntityPlayer entityPlayer, Player player, LocationRecord home) {
        var location = home.toLocation();
        if (location == null) {
            player.sendMessage(TextFormat.RED + "Home location is unavailable (missing world or dimension).");
            return;
        }

        boolean success = entityPlayer.teleport(location);
        if (success) {
            player.sendMessage(TextFormat.GREEN + "Teleported to home " + home.name() + "!");
        } else {
            player.sendMessage(TextFormat.RED + "Teleportation failed.");
        }
    }
}


