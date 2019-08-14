package net.zyuiop.rpmachine.projects.subcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.entities.AdminLegalEntity;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.entities.LegalEntityType;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.projects.ProjectsManager;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import java.util.Arrays;
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
    public boolean run(Player player, String command, String subCommand, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Arguments manquants : /project " + getUsage());
            return false;
        }

        String projectName = args[0];
        Project project = manager.getZone(projectName);
        if (project == null) {
            player.sendMessage(ChatColor.RED + "Ce projet n'existe pas.");
            return false;
        }

        try {
            LegalEntity e = LegalEntityType.getLegalEntity(player, "player", Arrays.copyOfRange(args, 1, args.length));
            Messages.sendMessage(project.owner(), ChatColor.YELLOW + "Le projet " + project.shortDisplayable() + ChatColor.YELLOW + " est transféré à " + e.displayable());
            Messages.sendMessage(e, ChatColor.YELLOW + "Vous êtes désormais propriétaire du projet " + project.shortDisplayable() + ChatColor.YELLOW + " !");
            project.setOwner(e);
            player.sendMessage(ChatColor.YELLOW + "Projet " + project.shortDisplayable() + ChatColor.YELLOW + " transféré à " + e.displayable());
            project.save();
        } catch (CommandException e) {
            player.sendMessage(ChatColor.RED + e.getMessage());
        }
        return true;
    }
}
