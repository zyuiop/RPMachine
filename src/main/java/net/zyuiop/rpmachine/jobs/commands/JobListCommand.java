package net.zyuiop.rpmachine.jobs.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.jobs.Job;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * @author Louis Vialar
 */
public class JobListCommand implements SubCommand {
    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "liste les métiers disponibles";
    }

    @Override
    public boolean run(Player commandSender, String command, String subCommand, String[] args) {
        commandSender.sendMessage(ChatColor.YELLOW + "Voici la liste des métiers que vous pouvez choisir :");
        Map<Job, Boolean> available = RPMachine.getInstance().getJobsManager().getAvailableJobs();


        for (Map.Entry<Job, Boolean> entry : available.entrySet()) {
            Job job = entry.getKey();
            ChatColor color = entry.getValue() ? ChatColor.GREEN : ChatColor.RED;

            commandSender.sendMessage(ChatColor.YELLOW + "- " + color + job.getJobName() + ChatColor.YELLOW + " : " + ChatColor.WHITE + job.getJobDescription());
        }

        commandSender.sendMessage(ChatColor.GRAY + "En " + ChatColor.GREEN + "vert" + ChatColor.GRAY + " les métiers que vous pouvez choisir, en " + ChatColor.RED + "rouge" + ChatColor.GRAY + " les métiers déjà trop choisis");
        return true;
    }
}
