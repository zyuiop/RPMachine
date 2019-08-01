package net.zyuiop.rpmachine.economy.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.AbstractCommand;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.economy.jobs.Job;
import net.zyuiop.rpmachine.economy.jobs.JobRestrictions;
import net.zyuiop.rpmachine.shops.types.AbstractShopSign;
import net.zyuiop.rpmachine.shops.types.EnchantingSign;
import net.zyuiop.rpmachine.shops.types.ItemShopSign;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public class CommandJob extends AbstractCommand {
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
                commandSender.sendMessage(ChatColor.GOLD + "Vous pouvez vendre/crafter les items suivants :");
                for (Material mat : j.getRestrictedItems())
                    commandSender.sendMessage(ChatColor.YELLOW + "- " + mat.toString());
                if (!j.getRestrictedBlocks().isEmpty()) {
                    commandSender.sendMessage(ChatColor.GOLD + "Vous pouvez placer/utiliser les blocs suivants :");
                    for (Material mat : j.getRestrictedBlocks())
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

                new Thread(() -> {
                    PlayerData data = RPMachine.database().getPlayerData(((Player) commandSender).getUniqueId());
                    if (data.getJob() != null)
                        commandSender.sendMessage(ChatColor.RED + "Vous avez déjà un métier.");
                    else {
                        data.setJob(j.getJobName());
                        commandSender.sendMessage(ChatColor.GREEN + "Vous avez bien choisi le métier " + ChatColor.DARK_GREEN + j.getJobName());
                    }
                }).start();
            } else if (com.equalsIgnoreCase("quit")) {
                // Get shops
                List<AbstractShopSign> signs = rpMachine.getShopsManager().getPlayerShops(commandSender).stream().filter(shop -> {
                    if (shop instanceof ItemShopSign)
                        return ((ItemShopSign) shop).getAction() == ItemShopSign.ShopAction.SELL && rpMachine.getJobsManager().isItemRestricted(((ItemShopSign) shop).getItemType());
                    else if (shop instanceof EnchantingSign)
                        return RPMachine.getInstance().getJobsManager().isRestrictionEnabled(JobRestrictions.ENCHANTING);
                    return false;
                }).collect(Collectors.toList());

                // TODO: destroy enchantment shops
                if (strings.length < 2) {
                    if (signs.isEmpty()) {
                        commandSender.sendMessage(ChatColor.RED + "Voulez vous vraiment changer de métier ? Merci de confirmer l'opération avec /jobs quit confirm");
                    } else {
                        commandSender.sendMessage(ChatColor.RED + "ATTENTION : L'utilisation de cette commande va déclencher la destruction des shops suivants. Merci de confirmer l'opération avec /jobs quit confirm");
                        for (AbstractShopSign s : signs) {
                            commandSender.sendMessage(ChatColor.GOLD + "- " + s.describe());
                        }
                        commandSender.sendMessage(ChatColor.GRAY + "Vos autres boutiques ne seront pas affectées.");
                    }
                    return true;
                } else if (strings[1].equalsIgnoreCase("confirm")) {
                    int i = 0;
                    for (AbstractShopSign sign : signs) {
                        sign.breakSign((Player) commandSender);
                        i++;
                    }
                    commandSender.sendMessage(ChatColor.AQUA + "" + i + ChatColor.GOLD + " Shops ont été supprimés.");
                    new Thread(() -> {
                        PlayerData data = RPMachine.database().getPlayerData(((Player) commandSender).getUniqueId());
                        data.setJob(null);
                        commandSender.sendMessage(ChatColor.GOLD + "Vous n'avez maintenant plus de métier.");
                    }).start();
                }
            }
        }
        return true;
    }
}
