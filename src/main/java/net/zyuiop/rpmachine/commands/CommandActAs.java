package net.zyuiop.rpmachine.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.entities.LegalEntityType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zyuiop
 */
public class CommandActAs extends AbstractCommand {
    public CommandActAs() {
        super("actas", null);
    }

    @Override
    protected boolean onPlayerCommand(Player player, String command, String[] args) {
        try {
            LegalEntity e = LegalEntityType.getLegalEntity(player, "me", args);

            if (!e.canActAs(player)) {
                player.sendMessage(ChatColor.RED + "Vous ne pouvez pas agir en tant que " + e.displayable());
            } else {
                RPMachine.setPlayerRoleToken(player, e);
                player.sendMessage(ChatColor.GREEN + "Vous agissez désormais en tant que : " + e.displayable());
            }
        } catch (CommandException e) {
            player.sendMessage(ChatColor.RED + e.getMessage());
        }

        return false;
    }

    @Override
    protected List<String> onPlayerTabComplete(Player player, String command, String... args) {
        if (args.length > 1)
            return null;
        Set<String> subCommands = LegalEntityType.getAliases();

        return subCommands.stream()
                .filter(key -> args.length == 0 || key.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }
}
