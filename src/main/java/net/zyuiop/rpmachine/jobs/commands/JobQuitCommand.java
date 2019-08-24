package net.zyuiop.rpmachine.jobs.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.ConfirmationCommand;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.jobs.Job;
import net.zyuiop.rpmachine.jobs.JobRestrictions;
import net.zyuiop.rpmachine.shops.types.AbstractShopSign;
import net.zyuiop.rpmachine.shops.types.EnchantingSign;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Louis Vialar
 */
public class JobQuitCommand implements SubCommand, ConfirmationCommand {
    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "quitte votre métier actuel";
    }

    @Override
    public boolean canUse(Player player) {
        Job j = RPMachine.getInstance().getJobsManager().getJob(player);
        return j != null;
    }

    @Override
    public boolean run(Player commandSender, String command, String subCommand, String[] strings) {
        PlayerData data = RPMachine.database().getPlayerData(commandSender.getUniqueId());
        if (data.hasAttribute(CommandJob.ATTRIBUTE_LAST_CHANGE)) {
            Long lastChange = data.getAttribute(CommandJob.ATTRIBUTE_LAST_CHANGE);
            long nextChange = lastChange + 24L * 3600L * 1000L * RPMachine.getInstance().getJobsManager().getQuitFrequency();

            if (nextChange > System.currentTimeMillis()) {
                commandSender.sendMessage(ChatColor.RED + "Vous ne pouvez pas quitter votre métier avant le " + ChatColor.DARK_RED + DateFormat.getDateTimeInstance().format(new Date(nextChange)));
                return true;
            }
        }

        // Get shops
        List<AbstractShopSign> signs = RPMachine.getInstance().getShopsManager().getPlayerShops(commandSender).stream().filter(shop -> {
            if (shop instanceof EnchantingSign)
                return RPMachine.getInstance().getJobsManager().isRestrictionEnabled(JobRestrictions.ENCHANTING);
            return false;
        }).collect(Collectors.toList());

        // TODO: maybe adapt to confirmation command via new method
        if (strings.length < 2) {
            if (signs.isEmpty()) {
                commandSender.sendMessage(ChatColor.RED + "Voulez vous vraiment quitter votre métier ? Merci de confirmer l'opération avec /jobs quit confirm");
            } else {
                commandSender.sendMessage(ChatColor.RED + "ATTENTION : L'utilisation de cette commande va déclencher la destruction des shops suivants. Merci de confirmer l'opération avec /jobs quit confirm");
                for (AbstractShopSign s : signs) {
                    commandSender.sendMessage(ChatColor.GOLD + "- " + s.describe());
                }
                commandSender.sendMessage(ChatColor.GRAY + "Vos autres boutiques ne seront pas affectées.");
            }
            commandSender.sendMessage(ChatColor.YELLOW + "L'abandon de votre métier coûtera " + ChatColor.AQUA + RPMachine.getInstance().getJobsManager().getQuitPrice() + RPMachine.getCurrencyName());
            return true;
        } else if (strings[1].equalsIgnoreCase("confirm")) {
            int i = 0;
            for (AbstractShopSign sign : signs) {
                sign.breakSign((Player) commandSender);
                i++;
            }
            commandSender.sendMessage(ChatColor.AQUA + "" + i + ChatColor.GOLD + " Shops ont été supprimés.");
            data.setJob(null);
            data.setAttribute(CommandJob.ATTRIBUTE_LAST_CHANGE, System.currentTimeMillis());
            commandSender.sendMessage(ChatColor.GOLD + "Vous n'avez maintenant plus de métier.");
        }
        return true;
    }
}
