package me.daoge.essentials.command;

import org.allaymc.api.command.Command;
import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandNode;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.utils.TextFormat;

/**
 * Ping command - displays player's current latency
 * 
 * @author daoge
 */
public class PingCommand extends Command {
    
    public PingCommand() {
        super("ping", "Display your current latency", "essentials.command.ping");
    }
    
    @Override
    public void prepareCommandTree(CommandTree tree) {
        CommandNode root = tree.getRoot();
        
        root.exec((context, player) -> {
                int ping = player.getController().getPing();
                TextFormat color;
                if (ping < 50) {
                    color = TextFormat.GREEN; // Green for low ping
                } else if (ping < 100) {
                    color = TextFormat.YELLOW; // Yellow for medium ping
                } else {
                    color = TextFormat.RED; // Red for high ping
                }
                
                context.addOutput("Your ping: " + color + ping + "ms");
                
                return context.success();
            }, SenderType.ACTUAL_PLAYER);
    }
}
