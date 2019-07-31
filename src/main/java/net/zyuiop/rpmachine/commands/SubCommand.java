package net.zyuiop.rpmachine.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * @author Louis Vialar
 */
public interface SubCommand {
    String getUsage();
    String getDescription();
    default boolean canUse(Player player) {
        return true;
    }
    boolean run(Player sender, String[] args);
    default boolean hasHelp() {
        return true;
    }

    default List<String> tabComplete(Player player, String[] args) {
        return null;
    }
}
