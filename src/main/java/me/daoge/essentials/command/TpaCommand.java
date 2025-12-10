package me.daoge.essentials.command;

import org.allaymc.api.command.Command;
import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandNode;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.form.Forms;
import org.allaymc.api.form.type.ModalForm;
import org.allaymc.api.permission.OpPermissionCalculator;
import org.allaymc.api.player.Player;
import org.allaymc.api.player.PlayerManager;
import org.allaymc.api.server.Server;
import org.allaymc.api.utils.TextFormat;

import java.util.*;

/**
 * TPA command - allows players to request teleportation to another player
 *
 * @author daoge
 */
public class TpaCommand extends Command {

    // Store pending TPA requests: targetPlayer -> requesterPlayer
    private static final Map<UUID, UUID> pendingRequests = new HashMap<>();

    public TpaCommand() {
        super("tpa", "Request teleportation to another player", "essentials.command.tpa");
        OpPermissionCalculator.NON_OP_PERMISSIONS.addAll(this.permissions);
    }

    /**
     * Remove a pending request (useful when player disconnects)
     */
    public static void removePendingRequest(UUID playerUUID) {
        pendingRequests.remove(playerUUID);
        // Also remove if this player is a requester
        pendingRequests.entrySet().removeIf(entry -> entry.getValue().equals(playerUUID));
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        CommandNode root = tree.getRoot();

        root.playerTarget("player")
                .exec((context, entityPlayer) -> {
                    // Get Player from EntityPlayer
                    Player player = entityPlayer.getController();

                    if (player == null) {
                        return context.fail();
                    }

                    // Get target player from playerTarget result
                    List<EntityPlayer> targetEntities = context.getResult(0);
                    if (targetEntities == null || targetEntities.isEmpty()) {
                        context.addPlayerNotFoundError();
                        return context.fail();
                    }

                    // Check if multiple players matched
                    if (targetEntities.size() > 1) {
                        context.addTooManyTargetsError();
                        return context.fail();
                    }

                    // Get the target player
                    EntityPlayer targetEntity = targetEntities.get(0);
                    Player target = targetEntity.getController();

                    if (target == null) {
                        context.addPlayerNotFoundError();
                        return context.fail();
                    }

                    if (target.equals(player)) {
                        context.addError("You cannot teleport to yourself!");
                        return context.fail();
                    }

                    UUID requesterUUID = player.getLoginData().getUuid();
                    UUID targetUUID = target.getLoginData().getUuid();

                    // Check if there's already a pending request
                    if (pendingRequests.containsKey(targetUUID)) {
                        context.addError("There is already a pending TPA request for %s!", target.getOriginName());
                        return context.fail();
                    }

                    // Store the request
                    pendingRequests.put(targetUUID, requesterUUID);

                    // Send notification to requester
                    context.addOutput(TextFormat.GREEN + "TPA request sent to %s!", target.getOriginName());

                    // Send notification to target
                    target.sendMessage(TextFormat.YELLOW + player.getOriginName() + " wants to teleport to you!");

                    // Create and show ModalForm to target
                    ModalForm form = Forms.modal()
                            .title("TPA Request")
                            .content(player.getOriginName() + " wants to teleport to you.\nDo you want to accept?")
                            .trueButton("Accept")
                            .falseButton("Deny")
                            .onTrue(() -> {
                                handleTpaResponse(targetUUID, requesterUUID, true);
                            })
                            .onFalse(() -> {
                                handleTpaResponse(targetUUID, requesterUUID, false);
                            })
                            .onClose(() -> {
                                // If form is closed without response, remove the request
                                pendingRequests.remove(targetUUID);
                            });

                    target.viewForm(form);

                    return context.success();
                }, SenderType.ACTUAL_PLAYER);
    }

    private void handleTpaResponse(UUID targetUUID, UUID requesterUUID, boolean accepted) {
        pendingRequests.remove(targetUUID);

        PlayerManager playerManager = Server.getInstance().getPlayerManager();
        Player target = playerManager.getPlayers().get(targetUUID);
        Player requester = playerManager.getPlayers().get(requesterUUID);

        if (requester == null) {
            if (target != null) {
                target.sendMessage(TextFormat.RED + "The player who requested TPA is no longer online.");
            }
            return;
        }

        if (target == null) {
            requester.sendMessage(TextFormat.RED + "The target player is no longer online.");
            return;
        }

        if (accepted) {
            // Teleport requester to target
            if (requester.getControlledEntity() != null && target.getControlledEntity() != null) {
                boolean success = requester.getControlledEntity().teleport(target.getControlledEntity().getLocation());
                if (success) {
                    requester.sendMessage(TextFormat.GREEN + "Teleporting to " + target.getOriginName() + "!");
                    target.sendMessage(TextFormat.GREEN + requester.getOriginName() + " has been teleported to you!");
                } else {
                    requester.sendMessage(TextFormat.RED + "Teleportation failed!");
                    target.sendMessage(TextFormat.RED + "Teleportation failed!");
                }
            } else {
                requester.sendMessage(TextFormat.RED + "Cannot teleport: entity not found!");
                if (target != null) {
                    target.sendMessage(TextFormat.RED + "Teleportation failed: entity not found!");
                }
            }
        } else {
            requester.sendMessage(TextFormat.RED + "Your TPA request was denied.");
            target.sendMessage(TextFormat.YELLOW + "You denied the TPA request from " + requester.getOriginName() + ".");
        }
    }
}
