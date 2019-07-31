package net.zyuiop.rpmachine.projects;

import net.zyuiop.rpmachine.commands.CompoundCommand;
import net.zyuiop.rpmachine.projects.subcommands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class ProjectCommand extends CompoundCommand {
	public ProjectCommand(ProjectsManager manager) {
		super("project", null, "projet", "projets", "projects", "zone", "zones", "z");

		// Enregistrement des commandes
		registerSubCommand("create", new CreateCommand(manager), "c");
		registerSubCommand("members", new MembersCommand(manager));
		registerSubCommand("remove", new RemoveCommand(manager));
		registerSubCommand("leave", new LeaveCommand(manager));
		registerSubCommand("wand", new WandCommand(), "w");
		registerSubCommand("redefine", new RedefineCommand(manager));
		registerSubCommand("info", new InfoCommand(manager), "i");
		registerSubCommand("setowner", new SetOwnerCommand(manager));
	}
}
