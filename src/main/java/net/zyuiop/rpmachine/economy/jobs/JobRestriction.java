package net.zyuiop.rpmachine.economy.jobs;

import net.zyuiop.rpmachine.RPMachine;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.stream.Collectors;

/**
 * @author Louis Vialar
 */
public abstract class JobRestriction implements Listener {
    private final JobRestrictions restrictions = JobRestrictions.byClass(this);

    protected boolean isAllowed(Player player) {
        Job job = RPMachine.getInstance().getJobsManager().getJob(player);

        boolean allow = job != null && job.restrictions.contains(restrictions);

        if (!allow) {
            String availableJobs =
                    StringUtils.join(RPMachine.getInstance().getJobsManager().getJobs(restrictions).stream().map(Job::getJobName).collect(Collectors.toList()),
                            ChatColor.GOLD + ", " + ChatColor.YELLOW);

            player.sendMessage(ChatColor.RED + "Cette action est restreinte et nécessite d'avoir le travail adéquat. Métiers autorisés : " + ChatColor.YELLOW + availableJobs);
        }

        return allow;
    }
}
