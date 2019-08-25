package net.zyuiop.rpmachine.jobs.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.jobs.Job;
import net.zyuiop.rpmachine.jobs.JobRestrictions;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
public class JobPlayersCommand implements SubCommand {
    @Override
    public String getUsage() {
        return "<métier>";
    }

    @Override
    public String getDescription() {
        return "liste les joueurs actifs pratiquant un métier";
    }

    @Override
    public boolean run(Player commandSender, String command, String subCommand, String[] args) {
        if (args.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "Utilisation : /" + command + " " + subCommand + " <métier>");
            return true;
        }

        String job = args[0];
        Job j = RPMachine.getInstance().getJobsManager().getJob(job);
        if (j == null) {
            commandSender.sendMessage(ChatColor.RED + "Ce métier n'a pas été trouvé.");
            return true;
        }

        commandSender.sendMessage(ChatColor.GOLD + "Liste des joueurs actifs pratiquant le métier " + ChatColor.YELLOW + j.getJobName());
        RPMachine.getInstance().getDatabaseManager().getActivePlayers().stream()
                .filter(p -> p.getJob() != null && p.getJob().equalsIgnoreCase(j.getJobName()))
                .map(p -> RPMachine.getInstance().getDatabaseManager().getUUIDTranslator().getName(p.getUuid()))
                .forEach(name -> commandSender.sendMessage(ChatColor.GRAY + " - " + ChatColor.YELLOW + name));
        return true;
    }
}
