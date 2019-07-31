package net.zyuiop.rpmachine.projects.subcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.economy.AdminAccountHolder;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SetOwnerCommand implements SubCommand {
    private final ProjectsManager manager;

    public SetOwnerCommand(ProjectsManager manager) {
        this.manager = manager;
    }

    @Override
    public String getUsage() {
        return "<nom> <player <name>|admin|project <name>|city <name>>";
    }

    @Override
    public String getDescription() {
        return "change le propriétaire du projet pour un joueur, un projet, une ville, ou l'admin";
    }

    @Override
    public boolean canUse(Player player) {
        return player.hasPermission("zones.setowner");
    }

    @Override
    public boolean run(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Arguments manquants : /project " + getUsage());
            return false;
        }

        String projectName = args[0];
        String ownerType = args[1];
        String ownerData = (args.length > 2) ? args[2] : null;

        Project project = manager.getZone(projectName);
        if (project == null) {
            player.sendMessage(ChatColor.RED + "Ce projet n'existe pas.");
            return false;
        }

        if (ownerType.equalsIgnoreCase("admin")) {
            project.setOwner(AdminAccountHolder.INSTANCE);
        } else if (ownerData == null) {
            player.sendMessage(ChatColor.RED + "Vous devez fournir le nom d'une entité.");
            return false;
        } else if (ownerType.equalsIgnoreCase("player")) {
            UUID playerId = RPMachine.database().getUUIDTranslator().getUUID(ownerData);
            if (playerId == null)
                player.sendMessage(ChatColor.RED + "Ce joueur n'existe pas !");
            else
                project.setOwner(RPMachine.database().getPlayerData(playerId));
        } else if (ownerType.equalsIgnoreCase("project")) {
            Project parentProject = manager.getZone(projectName);
            if (parentProject == null)
                player.sendMessage(ChatColor.RED + "Ce projet n'existe pas !");
            else
                project.setOwner(parentProject);
        } else if (ownerType.equalsIgnoreCase("city")) {
            City city = RPMachine.getInstance().getCitiesManager().getCity(ownerData);
            if (city == null)
                player.sendMessage(ChatColor.RED + "Cette ville n'existe pas !");
            else
                project.setOwner(city);
        }

        player.sendMessage(ChatColor.GREEN + "Projet mis à jour.");
        project.save();

        return true;
    }
}
