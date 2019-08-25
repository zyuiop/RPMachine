package net.zyuiop.rpmachine.jobs.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.ConfirmationCommand;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.jobs.Job;
import net.zyuiop.rpmachine.jobs.JobRestrictions;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
public class JobChooseCommand implements SubCommand, ConfirmationCommand {
    @Override
    public String getUsage() {
        return "<métier>";
    }

    @Override
    public String getDescription() {
        return "rejoint un métier";
    }

    @Override
    public boolean run(Player commandSender, String command, String subCommand, String[] strings) {
        if (strings.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "Utilisation : /jobs choose <métier>");
            return true;
        }

        String job = strings[0];
        Job j = RPMachine.getInstance().getJobsManager().getJob(job);
        if (j == null) {
            commandSender.sendMessage(ChatColor.RED + "Ce métier n'a pas été trouvé.");
            return true;
        }

        PlayerData data = RPMachine.database().getPlayerData(commandSender);
        if (data.getJob() != null)
            commandSender.sendMessage(ChatColor.RED + "Vous avez déjà un métier. " + ChatColor.YELLOW + "/jobs quit" + ChatColor.RED + " pour le quitter.");
        else {

            if (requestConfirm(commandSender,
                    ChatColor.YELLOW + "Voulez vous vraiment adopter le métier " + ChatColor.GOLD + j.getJobName() + ChatColor.YELLOW + " ? " +
                            "Vous ne pourrez pas changer avant " + ChatColor.GOLD + RPMachine.getInstance().getJobsManager().getQuitFrequency() + " jours " + ChatColor.YELLOW +
                            " et le prochain changement coûtera " + ChatColor.AQUA + RPMachine.getInstance().getJobsManager().getQuitPrice() + RPMachine.getCurrencyName(),
                    command + " " + subCommand, strings)) {
                data.setJob(j.getJobName());
                data.setAttribute(CommandJob.ATTRIBUTE_LAST_CHANGE, System.currentTimeMillis());

                commandSender.sendMessage(ChatColor.GREEN + "Vous avez bien choisi le métier " + ChatColor.DARK_GREEN + j.getJobName());
            }
        }
        return true;
    }
}
