package net.zyuiop.rpmachine.cities.commands.citysubcommands.params;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class SetVATCommand implements CityMemberSubCommand {

    private final CitiesManager citiesManager;

    public SetVATCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "[taxe sur la valeur ajoutée]";
    }

    @Override
    public String getDescription() {
        return "modifie la valeur de la TVA, perçue par la ville sur toutes les ventes faites par des shops automatisés dans la ville";
    }

    @Override
    public Permission requiresPermission() {
        return CityPermissions.SET_TAXES;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String command, String subcommand, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "TVA actuelle : " + ChatColor.GOLD + ((100*city.getVat())) + " %");
            return true;
        } else {
            try {
                double value = Double.parseDouble(args[0]) / 100D;
                if (value > .5D) {
                    player.sendMessage(ChatColor.RED + "Votre taux est supérieur à 50% !");
                    return true;
                }
                city.setVat(value);
                citiesManager.saveCity(city);
                player.sendMessage(ChatColor.GREEN + "La TVA est désormais de " + ChatColor.DARK_GREEN + ((value * 100)) + " %");
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "Le montant est incorrect.");
            }
            return true;
        }
    }
}
