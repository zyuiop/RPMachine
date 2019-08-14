package net.zyuiop.rpmachine.cities.commands.citysubcommands.params;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class SetSellTaxCommand implements CityMemberSubCommand {

    private final CitiesManager citiesManager;

    public SetSellTaxCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "[taxe de revente]";
    }

    @Override
    public String getDescription() {
        return "modifie le taux versé à la ville lors de la vente d'une parcelle";
    }

    @Override
    public Permission requiresPermission() {
        return CityPermissions.SET_TAXES;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String command, String subcommand, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "Taxe de vente de parcelle actuelle : " + ChatColor.GOLD + ((int) (100*city.getPlotSellTaxRate())) + " %");
            return true;
        } else {
            try {
                double value = Integer.parseInt(args[0]) / 100D;
                if (value > 1.0D) {
                    player.sendMessage(ChatColor.RED + "Votre taux est supérieur à 100% !");
                    return true;
                }
                city.setPlotSellTaxRate(value);
                citiesManager.saveCity(city);
                player.sendMessage(ChatColor.GREEN + "La taxe de vente de parcelle est désormais de " + ChatColor.DARK_GREEN + ((int) (value * 100)) + " %");
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Le montant est incorrect.");
            }
            return true;
        }
    }
}
