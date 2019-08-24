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
public class JobStatsCommand implements SubCommand {
    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "affiche des statistiques sur les métiers";
    }

    @Override
    public boolean run(Player commandSender, String command, String subCommand, String[] args) {
        commandSender.sendMessage(ChatColor.YELLOW + "Voici la liste des métiers avec leur nombre et proportion :");
        Map<Job, Long> q = RPMachine.getInstance().getJobsManager().getJobsQuantities();
        Map<Job, Double> p = RPMachine.getInstance().getJobsManager().getJobsProportion();

        for (Map.Entry<Job, Long> entry : q.entrySet()) {
            Job job = entry.getKey();
            Long quantity = entry.getValue();
            Double prop = p.get(job);

            commandSender.sendMessage(ChatColor.YELLOW + "- " + job.getJobName() + " : " + ChatColor.YELLOW + quantity +
                    ChatColor.WHITE + " utilisateurs (" + ChatColor.YELLOW + String.format("%.2f", prop * 100) + " %" + ChatColor.WHITE + ")");
        }

        commandSender.sendMessage(ChatColor.GRAY + "Lorsque le total d'utilisateurs actifs ayant un travail est inférieur à 15, les pourcentages sont forcés à 0.");
        return true;
    }
}
