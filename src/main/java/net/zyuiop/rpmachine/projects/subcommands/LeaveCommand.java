package net.zyuiop.rpmachine.projects.subcommands;

import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.economy.AdminTaxPayer;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class LeaveCommand implements SubCommand {
    private final ProjectsManager manager;

    public LeaveCommand(ProjectsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getUsage() {
        return "<zone>";
    }

    @Override
    public String getDescription() {
        return "quitte le projet choisi";
    }

    @Override
    public boolean run(Player player, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Utilisation : /project leave " + getUsage());
            return false;
        }

        Project plot = manager.getZone(args[0]);
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "Ce projet n'existe pas.");
        } else if (!plot.getOwner().getLandOwner().canManagePlot(player)) {
            player.sendMessage(ChatColor.RED + "Ce projet ne vous appartient pas.");
        } else {
            plot.setOwner(AdminTaxPayer.INSTANCE);
            manager.saveZone(plot);
            player.sendMessage(ChatColor.RED + "Vous n'êtes plus propriétaire de ce projet");
        }

        return true;
    }
}
