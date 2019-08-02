package net.zyuiop.rpmachine.projects.subcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.permissions.ProjectPermissions;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MembersCommand implements SubCommand {
    private final ProjectsManager manager;

    public MembersCommand(ProjectsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getUsage() {
        return "<projet> <add|remove> <joueur>";
    }

    @Override
    public String getDescription() {
        return "ajoute ou supprime un membre à votre projet";
    }

    @Override
    public boolean run(Player player, String command, String subCommand, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Utilisation : /project members " + getUsage());
            return false;
        }

        Project plot = manager.getZone(args[0]);
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "Ce projet n'existe pas.");
        } else {
            UUID id = RPMachine.database().getUUIDTranslator().getUUID(args[2]);
            if (id == null) {
                player.sendMessage(ChatColor.RED + "Ce joueur n'a pas été trouvé.");
            } else {
                if (args[1].equalsIgnoreCase("add")) {
                    if (!plot.hasPermission(player, ProjectPermissions.ADD_NEW_MEMBER)) {
                        player.sendMessage(ChatColor.RED + "Ce projet ne vous appartient pas.");
                        return true;
                    }

                    if (plot.getPlotMembers().contains(id)) {
                        player.sendMessage(ChatColor.GREEN + "Ce joueur est déjà dans le projet.");
                        return true;
                    }
                    plot.getPlotMembers().add(id);
                    manager.saveZone(plot);
                    player.sendMessage(ChatColor.GREEN + "Le joueur a été ajouté dans le projet.");
                } else if (args[1].equalsIgnoreCase("remove")) {

                    if (!plot.hasPermission(player, ProjectPermissions.REMOVE_MEMBER)) {
                        player.sendMessage(ChatColor.RED + "Ce projet ne vous appartient pas.");
                        return true;
                    }

                    if (!plot.getPlotMembers().contains(id)) {
                        player.sendMessage(ChatColor.GREEN + "Ce joueur n'est pas dans le projet.");
                        return true;
                    }
                    plot.getPlotMembers().remove(id);
                    manager.saveZone(plot);
                    player.sendMessage(ChatColor.GREEN + "Le joueur a été supprimé du projet.");
                } else {
                    player.sendMessage(ChatColor.RED + "Argument invalide (add / remove)");
                }
            }
        }
        return true;
    }
}
