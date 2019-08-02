package net.zyuiop.rpmachine.commands;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Louis Vialar
 */
public class CompoundSubCommand implements SubCommand, CompoundCommandBase {
    private final String usage;
    private final String description;
    private final Map<String, SubCommand> subCommandMap = new HashMap<>();

    public CompoundSubCommand(String usage, String description) {
        registerHelp();

        this.usage = usage;
        this.description = description;
    }

    public String getUsage() {
        return usage;
    }

    public String getDescription() {
        return description;
    }

    public boolean run(Player sender, String command, String subCommand, String[] args) {
        return runCommand(sender, command + " " + subCommand, args);
    }

    public List<String> tabComplete(Player player, String[] args) {
        return genTabComplete(player, args);
    }

    @Override
    public Map<String, SubCommand> subCommands() {
        return subCommandMap;
    }

    @Override
    public boolean canUse(Player player) {
        return subCommandMap.values().stream().anyMatch(com -> com.canUse(player));
    }
}
