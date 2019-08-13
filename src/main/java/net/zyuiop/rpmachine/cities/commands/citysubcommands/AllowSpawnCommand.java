package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.CityFloor;
import net.zyuiop.rpmachine.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AllowSpawnCommand implements SubCommand {

    private final CitiesManager citiesManager;

    public AllowSpawnCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "<ville> [yes/no]";
    }

    @Override
    public String getDescription() {
        return "autorise une ville à être le spawn du serveur";
    }

    @Override
    public boolean canUse(Player player) {
        return player.hasPermission("admin.setallowspawn");
    }

    private String toDisplayable(boolean status) {
        return status ? (ChatColor.GREEN + "peut être un spawn") : (ChatColor.RED + "ne peut pas être un spawn");
    }

    @Override
    public boolean run(Player player, String command, String subCommand, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Aucune ville indiquée.");
            return false;
        }

        City target = citiesManager.getCity(args[0]);

        if (target == null) {
            player.sendMessage(ChatColor.RED + "Aucune ville de ce nom là n'existe.");
            return false;
        }

        boolean isAllow = target.isAllowSpawn();

        if (args.length > 1) {
            String allow = args[1].toLowerCase();

            if (allow.equals("yes") || allow.equalsIgnoreCase("true")) {
                if (target.getSpawn() == null) {
                    player.sendMessage(ChatColor.RED + "La ville sélectionnée n'a pas de spawn.");
                    return false;
                }
                target.setAllowSpawn(true);
            } else if (allow.equals("no") || allow.equals("false")) {
                target.setAllowSpawn(false);
            } else {
                player.sendMessage(ChatColor.RED + "Paramètre incorrect " + allow + ". Valeurs aceptées: yes, no");
                return false;
            }

            player.sendMessage(ChatColor.YELLOW + "Statut modifié. La ville " + toDisplayable(target.isAllowSpawn()) + ChatColor.YELLOW + ". Statut précédent : " + toDisplayable(isAllow));
        } else {
            player.sendMessage(ChatColor.YELLOW + "La ville " + toDisplayable(isAllow));
        }


        if (target != null) {
            player.sendMessage(ChatColor.YELLOW + "-----[ Ville de " + ChatColor.GOLD + target.getCityName() + ChatColor.YELLOW + " ]-----");
            player.sendMessage(ChatColor.YELLOW + "Maire actuel : " + RPMachine.database().getUUIDTranslator().getName(target.getMayor()));
            player.sendMessage(ChatColor.YELLOW + "Nombre d'habitants : " + target.countInhabitants());
            player.sendMessage(ChatColor.YELLOW + "Type de ville : " + ((target.isRequireInvite() ? ChatColor.RED + "Sur invitation" : ChatColor.GREEN + "Publique")));
            player.sendMessage(ChatColor.YELLOW + "Impôts : " + target.getTaxes() + " " + RPMachine.getCurrencyName() + " par semaine");
            player.sendMessage(ChatColor.YELLOW + "Taxe de vente de parcelle : " + ((int) (100 * target.getPlotSellTaxRate())) + " %");
            player.sendMessage(ChatColor.YELLOW + "T.V.A. : " + 100 * target.getVat() + " %");

            if (!target.getInhabitants().contains(player.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "Taxe de citoyenneté : " + target.getJoinTax() + " " + RPMachine.getCurrencyName());

                if (RPMachine.isTpEnabled())
                    player.sendMessage(ChatColor.YELLOW + "Taxe de téléportation : " + target.getTpTax() + " " + RPMachine.getCurrencyName());
            }

            CityFloor floor = citiesManager.getFloor(target);
            player.sendMessage(ChatColor.YELLOW + "Palier : " + floor.getName());

            if (player.getUniqueId().equals(target.getMayor()) || target.getCouncils().contains(player.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "Monnaie : " + target.getBalance() + " " + RPMachine.getCurrencyName());
                player.sendMessage(ChatColor.YELLOW + "Taille : " + target.getChunks().size() + " / " + floor.getMaxsurface());
            }
        }
        return true;
    }
}
