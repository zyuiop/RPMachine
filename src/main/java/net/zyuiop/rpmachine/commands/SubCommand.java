package net.zyuiop.rpmachine.commands;

import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Louis Vialar
 */
public interface SubCommand {
    String getUsage();

    String getDescription();

    default boolean canUse(Player player) {
        return true;
    }

    boolean run(Player sender, String command, String subCommand, String[] args);

    default boolean hasHelp() {
        return true;
    }

    default List<String> tabComplete(Player player, String[] args) {
        return null;
    }

    default List<String> tabCompleteHelper(@Nullable String arg, List<String> options) {
        if (arg == null) return options;
        else
            return options.stream().filter(opt -> opt.toLowerCase().startsWith(arg.toLowerCase())).collect(Collectors.toList());
    }
}
