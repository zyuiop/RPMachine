package net.zyuiop.rpmachine.jobs;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.AbstractCommand;
import net.zyuiop.rpmachine.commands.ConfirmationCommand;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.shops.types.AbstractShopSign;
import net.zyuiop.rpmachine.shops.types.EnchantingSign;
import net.zyuiop.rpmachine.shops.types.ItemShopSign;
import net.zyuiop.rpmachine.shops.types.ShopAction;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class CommandJob extends AbstractCommand implements ConfirmationCommand {
    private static final String ATTRIBUTE_LAST_CHANGE = "job.lastChange";

    public CommandJob() {
        super("job", null, "jobs");
    }

    @Override
    public boolean onPlayerCommand(Player commandSender, String command, String[] strings) {
        if (strings.length == 0) {
            commandSender.sendMessage(ChatColor.GOLD + "Un métier définit les types d'items que vous pouvez vendre.");
            commandSender.sendMessage(ChatColor.GOLD + "Listez les métiers disponibles : " + ChatColor.AQUA + "/jobs list");
            commandSender.sendMessage(ChatColor.GOLD + "Informations sur un métier : " + ChatColor.AQUA + "/jobs info <métier>");
            commandSender.sendMessage(ChatColor.GOLD + "Choisissez un métier : " + ChatColor.AQUA + "/jobs choose <métier>");
        } else {
            RPMachine rpMachine = RPMachine.getInstance();
            String com = strings[0];
            if (com.equalsIgnoreCase("list")) {
                commandSender.sendMessage(ChatColor.GOLD + "Voici la liste des métiers que vous pouvez choisir :");
                for (Job job : rpMachine.getJobsManager().getJobs().values())
                    commandSender.sendMessage("- " + ChatColor.DARK_AQUA + job.getJobName() + " : " + ChatColor.AQUA + job.getJobDescription());
            } else if (com.equalsIgnoreCase("info")) {
                if (strings.length < 2) {
                    commandSender.sendMessage(ChatColor.RED + "Utilisation : /jobs info <métier>");
                    return true;
                }

                String job = strings[1];
                Job j = rpMachine.getJobsManager().getJob(job);
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

            } else if (com.equalsIgnoreCase("choose")) {
                if (strings.length < 2) {
                    commandSender.sendMessage(ChatColor.RED + "Utilisation : /jobs choose <métier>");
                    return true;
                }

                String job = strings[1];
                Job j = rpMachine.getJobsManager().getJob(job);
                if (j == null) {
                    commandSender.sendMessage(ChatColor.RED + "Ce métier n'a pas été trouvé.");
                    return true;
                }

                PlayerData data = RPMachine.database().getPlayerData(((Player) commandSender).getUniqueId());
                if (data.getJob() != null)
                    commandSender.sendMessage(ChatColor.RED + "Vous avez déjà un métier. " + ChatColor.YELLOW + "/jobs quit" + ChatColor.RED + " pour le quitter.");
                else {

                    if (requestConfirm(commandSender,
                            ChatColor.YELLOW + "Voulez vous vraiment adopter le métier " + ChatColor.GOLD + j.getJobName() + ChatColor.YELLOW + " ? " +
                                    "Vous ne pourrez pas changer avant " + ChatColor.GOLD + rpMachine.getJobsManager().getQuitFrequency() + " jours " + ChatColor.YELLOW +
                                    " et le prochain changement coûtera " + ChatColor.AQUA + rpMachine.getJobsManager().getQuitPrice() + RPMachine.getCurrencyName(),
                            command, strings)) {
                        data.setJob(j.getJobName());
                        data.setAttribute(ATTRIBUTE_LAST_CHANGE, System.currentTimeMillis());

                        commandSender.sendMessage(ChatColor.GREEN + "Vous avez bien choisi le métier " + ChatColor.DARK_GREEN + j.getJobName());
                    }
                }
            } else if (com.equalsIgnoreCase("quit")) {
                // Can quit ?
                Job j = rpMachine.getJobsManager().getJob(commandSender);
                if (j == null) {
                    commandSender.sendMessage(ChatColor.RED + "Vous n'avez pas de métier.");
                    return true;
                }

                PlayerData data = RPMachine.database().getPlayerData(commandSender.getUniqueId());
                if (data.hasAttribute(ATTRIBUTE_LAST_CHANGE)) {
                    Long lastChange = data.getAttribute(ATTRIBUTE_LAST_CHANGE);
                    long nextChange = lastChange + 24L * 3600L * 1000L * rpMachine.getJobsManager().getQuitFrequency();

                    if (nextChange > System.currentTimeMillis()) {
                        commandSender.sendMessage(ChatColor.RED + "Vous ne pouvez pas quitter votre métier avant le " + ChatColor.DARK_RED + DateFormat.getDateTimeInstance().format(new Date(nextChange)));
                        return true;
                    }
                }

                // Get shops
                List<AbstractShopSign> signs = rpMachine.getShopsManager().getPlayerShops(commandSender).stream().filter(shop -> {
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
                    commandSender.sendMessage(ChatColor.YELLOW + "L'abandon de votre métier coûtera " + ChatColor.AQUA + rpMachine.getJobsManager().getQuitPrice() + RPMachine.getCurrencyName());
                    return true;
                } else if (strings[1].equalsIgnoreCase("confirm")) {
                    int i = 0;
                    for (AbstractShopSign sign : signs) {
                        sign.breakSign((Player) commandSender);
                        i++;
                    }
                    commandSender.sendMessage(ChatColor.AQUA + "" + i + ChatColor.GOLD + " Shops ont été supprimés.");
                    data.setJob(null);
                    data.setAttribute(ATTRIBUTE_LAST_CHANGE, System.currentTimeMillis());
                    commandSender.sendMessage(ChatColor.GOLD + "Vous n'avez maintenant plus de métier.");
                }
            }
        }
        return true;
    }
}
