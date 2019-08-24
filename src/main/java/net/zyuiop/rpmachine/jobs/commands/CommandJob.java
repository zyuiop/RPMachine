package net.zyuiop.rpmachine.jobs.commands;

import net.zyuiop.rpmachine.commands.CompoundCommand;
import net.zyuiop.rpmachine.commands.ConfirmationCommand;

public class CommandJob extends CompoundCommand implements ConfirmationCommand {
    static final String ATTRIBUTE_LAST_CHANGE = "job.lastChange";

    public CommandJob() {
        super("job", null, "jobs");

        registerSubCommand("choose", new JobChooseCommand());
        registerSubCommand("list", new JobListCommand());
        registerSubCommand("info", new JobInfoCommand());
        registerSubCommand("player", new JobPlayerCommand(), "user");
        registerSubCommand("players", new JobPlayersCommand(), "users");
        registerSubCommand("quit", new JobQuitCommand());
        registerSubCommand("stats", new JobStatsCommand());
    }
}
