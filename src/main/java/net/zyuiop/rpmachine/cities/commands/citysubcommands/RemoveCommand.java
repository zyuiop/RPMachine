package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.commands.ConfirmationCommand;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.shops.types.AbstractShopSign;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class RemoveCommand implements CityMemberSubCommand, ConfirmationCommand {
    private final CitiesManager citiesManager;

    public RemoveCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "supprime votre ville";
    }

    @Override
    public boolean requiresMayorPrivilege() {
        return true;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String command, String subcommand, String[] args) {
        if (city.getPlots().size() > 0) {
            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas supprimer la ville car elle contient des parcelles.");
            return true;
        }

        if (requestConfirm(player,
                ChatColor.RED + "ATTENTION ! Vous vous apprêtez à supprimer votre ville ! Elle ne sera plus protégée contre le grief ou le vol !",
                command + " " + subcommand,
                args)) {

            citiesManager.removeCity(city);

            RPMachine.getInstance().getShopsManager().getShops(city).forEach(AbstractShopSign::breakSign);

            player.sendMessage(ChatColor.RED + "Votre ville a été supprimée.");
        }
        return true;
    }
}
