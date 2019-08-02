package net.zyuiop.rpmachine.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
public interface ConfirmationCommand {
    default boolean requestConfirm(Player player, String reason, String command, String[] args) {
        if (args.length > 0 && args[args.length - 1].equalsIgnoreCase("confirm"))
            return true;

        if (!command.startsWith("/"))
            command = "/" + command;

        command += " " + StringUtils.join(args, " ") + " confirm";

        TextComponent click = new TextComponent("[Cliquez pour confirmer]");
        click.setColor(ChatColor.GREEN);
        click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(command).color(ChatColor.YELLOW).create()));
        click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));

        player.sendMessage(reason);
        player.spigot().sendMessage(click);

        return false;
    }
}
