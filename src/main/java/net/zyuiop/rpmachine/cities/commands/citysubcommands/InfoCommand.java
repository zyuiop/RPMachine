package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.cities.CityFloor;
import net.zyuiop.rpmachine.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class InfoCommand implements SubCommand {

    private final CitiesManager citiesManager;

    public InfoCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "[ville]";
    }

    @Override
    public String getDescription() {
        return "affiche les informations de la ville actuelle ou de celle passée en argument.";
    }

    @Override
    public boolean run(Player player, String command, String subCommand, String[] args) {
        City target;
        if (args.length > 0) {
            target = citiesManager.getCity(args[0]);
        } else {
            if (!player.getLocation().getWorld().getName().equalsIgnoreCase("world")) {
                player.sendMessage(ChatColor.RED + "Vous n'êtes dans aucune ville actuellement.");
                return false;
            }

            target = citiesManager.getCityHere(player.getLocation().getChunk());
            if (target == null) {
                player.sendMessage(ChatColor.RED + "Vous n'êtes dans aucune ville actuellement.");
                return false;
            }
        }

        if (target != null) {
            player.sendMessage(ChatColor.YELLOW + "-----[ Ville de " + ChatColor.GOLD + target.getCityName() + ChatColor.YELLOW + " ]-----");
            player.sendMessage(ChatColor.YELLOW + "Maire actuel : " + RPMachine.database().getUUIDTranslator().getName(target.getMayor()));
            player.sendMessage(ChatColor.YELLOW + "Nombre d'habitants : " + target.countInhabitants());
            player.sendMessage(ChatColor.YELLOW + "Type de ville : " + ((target.isRequireInvite() ? ChatColor.RED + "Sur invitation" : ChatColor.GREEN + "Publique")));
            player.sendMessage(ChatColor.YELLOW + "Impôts : " + target.getTaxes() + " " + RPMachine.getCurrencyName() + " par semaine");
            player.sendMessage(ChatColor.YELLOW + "Taxe de vente de parcelle : " + ((int) (100 * target.getPlotSellTaxRate())) + " %");
            player.sendMessage(ChatColor.YELLOW + "T.V.A. : " + 100 * target.getVat() + " %");
            player.sendMessage(ChatColor.YELLOW + "Système politique : " + target.getPoliticalSystem().getName());

            if (!target.getInhabitants().contains(player.getUniqueId())) {
                player.sendMessage(ChatColor.YELLOW + "Taxe de citoyenneté : " + target.getJoinTax() + " " + RPMachine.getCurrencyName());

                if (RPMachine.isTpEnabled())
                    player.sendMessage(ChatColor.YELLOW + "Taxe de téléportation : " + target.getTpTax() + " " + RPMachine.getCurrencyName());
            }

            CityFloor floor = citiesManager.getFloor(target);
            player.sendMessage(ChatColor.YELLOW + "Palier : " + floor.getName());

            if (player.getUniqueId().equals(target.getMayor()) || target.getCouncils().contains(player.getUniqueId()) || player.isOp()) {
                player.sendMessage(ChatColor.YELLOW + "Monnaie : " + target.getBalance() + " " + RPMachine.getCurrencyName());
                player.sendMessage(ChatColor.YELLOW + "Taille : " + target.getChunks().size() + " / " + floor.getMaxsurface());
            }
        }
        return true;
    }
}
