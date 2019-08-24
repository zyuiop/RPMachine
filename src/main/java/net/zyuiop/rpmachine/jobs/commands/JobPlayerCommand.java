package net.zyuiop.rpmachine.jobs.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.jobs.Job;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * @author Louis Vialar
 */
public class JobPlayerCommand implements SubCommand {
    @Override
    public String getUsage() {
        return "<joueur>";
    }

    @Override
    public String getDescription() {
        return "affiche le métier d'un joueur";
    }

    @Override
    public boolean run(Player commandSender, String command, String subCommand, String[] args) {
        if (args.length < 2) {
            commandSender.sendMessage(ChatColor.RED + "Utilisation : /" + command + " " + subCommand + " <joueur>");
            return true;
        }

        UUID targetId = RPMachine.getInstance().getDatabaseManager().getUUIDTranslator().getUUID(args[0]);
        if (targetId == null) {
            commandSender.sendMessage(ChatColor.RED + "Pseudo introuvable.");
            return true;
        }

        Job j = RPMachine.getInstance().getJobsManager().getJob(targetId);
        if (j == null) {
            commandSender.sendMessage(ChatColor.RED + "Ce joueur n'a aucun métier...");
            return true;
        }

        commandSender.sendMessage(ChatColor.YELLOW + args[0] + ChatColor.GRAY + " pratique le métier suivant : " + ChatColor.YELLOW + j.getJobName());
        return true;
    }
}
