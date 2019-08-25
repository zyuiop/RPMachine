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
public class JobInfoCommand implements SubCommand {
    @Override
    public String getUsage() {
        return "<métier>";
    }

    @Override
    public String getDescription() {
        return "affiche des informations sur un métier";
    }

    @Override
    public boolean run(Player commandSender, String command, String subCommand, String[] strings) {
        if (strings.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "Utilisation : /jobs info <métier>");
            return true;
        }

        String job = strings[0];
        Job j = RPMachine.getInstance().getJobsManager().getJob(job);
        if (j == null) {
            commandSender.sendMessage(ChatColor.RED + "Ce métier n'a pas été trouvé.");
            return true;
        }

        commandSender.sendMessage(ChatColor.GOLD + "Informations sur le métier " + ChatColor.YELLOW + j.getJobName());

        if (!j.getRestrictUse().isEmpty()) {
            commandSender.sendMessage(ChatColor.GOLD + "Vous pouvez crafter/placer/utiliser les blocs suivants :");
            for (Material mat : j.getRestrictUse())
                commandSender.sendMessage(ChatColor.YELLOW + "- " + mat.toString());
        }

        if (!j.getRestrictCraft().isEmpty()) {
            commandSender.sendMessage(ChatColor.GOLD + "Vous pouvez crafter et vendre les items suivants :");
            for (Material mat : j.getRestrictCraft())
                commandSender.sendMessage(ChatColor.YELLOW + "- " + mat.toString());
        }

        if (!j.getRestrictions().isEmpty()) {
            commandSender.sendMessage(ChatColor.GOLD + "Vous avez accès aux actions suivantes :");
            for (JobRestrictions r : j.getRestrictions())
                commandSender.sendMessage(ChatColor.YELLOW + "- " + r.toString());
        }
        return true;
    }
}
