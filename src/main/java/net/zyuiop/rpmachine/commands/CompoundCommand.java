package net.zyuiop.rpmachine.commands;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Louis Vialar
 */
public class CompoundCommand extends AbstractCommand implements CompoundCommandBase {
    private final String commandName;
    private final Map<String, SubCommand> subCommands = new HashMap<>();

    @Override
    public Map<String, SubCommand> subCommands() {
        return subCommands;
    }

    @Override
    protected List<String> onPlayerTabComplete(Player player, String command, String... args) {
        return genTabComplete(player, args);
    }

    {
        registerHelp();
    }

    public CompoundCommand(String command, String requiredPermission, String... aliases) {
        super(command, requiredPermission, aliases);
        this.commandName = command;
    }

    @Override
    protected boolean onPlayerCommand(Player player, String command, String[] args) {
        return runCommand(player, command, args);
    }
}
