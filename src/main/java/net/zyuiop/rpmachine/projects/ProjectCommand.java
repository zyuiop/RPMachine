package net.zyuiop.rpmachine.projects;

import net.zyuiop.rpmachine.common.commands.CompoundCommand;
import net.zyuiop.rpmachine.projects.subcommands.*;


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
