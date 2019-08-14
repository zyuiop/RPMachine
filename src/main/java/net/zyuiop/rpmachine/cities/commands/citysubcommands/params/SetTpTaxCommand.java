package net.zyuiop.rpmachine.cities.commands.citysubcommands.params;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class SetTpTaxCommand implements CityMemberSubCommand {

    private final CitiesManager citiesManager;

    public SetTpTaxCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "[taxe de téléportation]";
    }

    @Override
    public String getDescription() {
        return "modifie le prix payé pour se téléporter à votre ville";
    }

    @Override
    public Permission requiresPermission() {
        return CityPermissions.SET_TAXES;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String command, String subcommand, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "Taxe de téléportation actuelle : " + ChatColor.GOLD + city.getTpTax() + " " + RPMachine.getCurrencyName());
            return true;
        } else {
            try {
                int value = Integer.parseInt(args[0]);
                if (value > citiesManager.getFloor(city).getMaxTpTax()) {
                    player.sendMessage(ChatColor.RED + "Votre montant est supérieur au montant maximal pour votre palier.");
                    return true;
                }
                city.setTpTax(value);
                citiesManager.saveCity(city);
                player.sendMessage(ChatColor.GREEN + "Le coût de téléportation est désormais de " + ChatColor.DARK_GREEN + value + " " + RPMachine.getCurrencyName());
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Le montant est incorrect.");
            }
            return true;
        }
    }
}
