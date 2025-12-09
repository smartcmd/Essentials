package me.daoge.essentials.command;

import me.daoge.essentials.EssentialsPlugin;
import org.allaymc.api.command.Command;
import org.allaymc.api.command.SenderType;
import org.allaymc.api.command.tree.CommandNode;
import org.allaymc.api.command.tree.CommandTree;
import org.allaymc.api.entity.interfaces.EntityPlayer;
import org.allaymc.api.form.Forms;
import org.allaymc.api.form.type.SimpleForm;
import org.allaymc.api.player.Player;
import org.allaymc.api.utils.TextFormat;
import org.allaymc.api.utils.config.Config;
import org.allaymc.api.utils.config.ConfigSection;

/**
 * Notice command - displays server notice to players
 * Admins can update the notice content using /notice set
 *
 * @author daoge
 */
public class NoticeCommand extends Command {

    public NoticeCommand() {
        super("notice", "View or update server notice", "essentials.command.notice");
    }

    @Override
    public void prepareCommandTree(CommandTree tree) {
        CommandNode root = tree.getRoot();

        // /notice - view notice
        root.key("view").exec((context, entityPlayer) -> {
                Player player = entityPlayer.getController();
                if (player == null) {
                    return context.fail();
                }

                showNotice(player);
                return context.success();
            }, SenderType.ACTUAL_PLAYER);

        // /notice set <content> - update notice content
        root.key("set")
            .permission("essentials.command.notice.set")
            .msg("content")
            .exec((context, entityPlayer) -> {
                Player player = entityPlayer.getController();
                if (player == null) {
                    return context.fail();
                }

                String content = context.getResult(1);
                if (content == null || content.trim().isEmpty()) {
                    context.addError("Notice content cannot be empty!");
                    return context.fail();
                }

                // Update config
                Config config = EssentialsPlugin.getInstance().getConfig();
                ConfigSection noticeSection = config.getSection("notice");
                noticeSection.put("content", content);
                config.set("notice", noticeSection);
                config.save();

                player.sendMessage(TextFormat.GREEN + "Notice updated successfully!");
                player.sendMessage(TextFormat.GRAY + "New content: " + content);

                return context.success();
            }, SenderType.ACTUAL_PLAYER);
    }

    /**
     * Show the notice form to a player
     */
    public static void showNotice(Player player) {
        Config config = EssentialsPlugin.getInstance().getConfig();
        ConfigSection noticeSection = config.getSection("notice");

        String title = noticeSection.getString("title", "Server Notice");
        String content = noticeSection.getString("content", "Welcome to the server!");

        // Replace \n with actual newlines for display
        content = content.replace("\\n", "\n");

        SimpleForm form = Forms.simple()
            .title(title)
            .content(content)
            .button("OK")
            .onClick(button -> {
                // Just close the form
            })
            .onClose(() -> {
                // Form closed
            });

        player.viewForm(form);
    }
}
