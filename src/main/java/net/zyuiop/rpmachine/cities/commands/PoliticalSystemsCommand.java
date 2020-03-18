package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.cities.politics.PoliticalSystems;
import net.zyuiop.rpmachine.commands.AbstractCommand;
import net.zyuiop.rpmachine.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PoliticalSystemsCommand extends AbstractCommand implements SubCommand {
    // Command registered as a subcommand -- but the instanciation creates a command as well
    public PoliticalSystemsCommand() {
        super("politicalsystems", null, "ps");
    }

    @Override
    protected boolean onPlayerCommand(Player player, String command, String[] args) {
        return run(player, command, "", args);
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "liste les systèmes politiques";
    }

    @Override
    public boolean run(Player commandSender, String command, String subCommand, String[] args) {
        commandSender.sendMessage(ChatColor.GOLD + " -----[ Systèmes politiques ] -----");
        commandSender.sendMessage(ChatColor.YELLOW + "Voici la liste des systèmes politiques, avec leur description :");
        for (PoliticalSystems ps : PoliticalSystems.values()) {
            commandSender.sendMessage(ChatColor.YELLOW + " - " + ChatColor.GOLD + ps.instance.getName());
            commandSender.sendMessage(ChatColor.DARK_AQUA + "Description : " + ChatColor.AQUA + ps.instance.getDescription());
        }
        return true;
    }
}
