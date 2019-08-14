package net.zyuiop.rpmachine.projects.subcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import net.zyuiop.rpmachine.projects.ProjectsPermGui;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PermissionsCommand implements SubCommand {
    private final ProjectsManager manager;

    public PermissionsCommand(ProjectsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getUsage() {
        return "<projet> <pseudo>";
    }

    @Override
    public String getDescription() {
        return "modifie les permissions d'un des membres du projet";
    }

    @Override
    public boolean run(Player player, String command, String subCommand, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Pseudo ou projet manquant.");
            return false;
        }

        String projectName = args[0];
        Project project = manager.getZone(projectName);
        if (project == null) {
            player.sendMessage(ChatColor.RED + "Ce projet n'existe pas.");
            return false;
        }

        String name = args[1];
        UUID id = RPMachine.database().getUUIDTranslator().getUUID(name);

        if (!project.getPlotMembers().contains(id)) {
            player.sendMessage(ChatColor.RED + "Cette personne n'est pas encore membre du projet. Ajoutez là au projet pour poursuivre.");
            return true;
        }

        player.sendMessage(ChatColor.GREEN + "Modification des permissions de " + ChatColor.YELLOW + name);
        new ProjectsPermGui(player, id, name, project).open();
        return true;
    }
}
