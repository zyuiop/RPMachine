package net.zyuiop.rpmachine.cities.commands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.cities.CityPlot;
import net.zyuiop.rpmachine.cities.commands.plotsubcommands.*;
import net.zyuiop.rpmachine.commands.CompoundCommand;
import net.zyuiop.rpmachine.common.Plot;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.Arrays;


public class PlotCommand extends CompoundCommand {
    public PlotCommand(CitiesManager citiesManager) {
        super("plot", null, "plots", "p", "parcelles", "parcelle");

        // Enregistrement des commandes
        registerSubCommand("create", new CreateCommand(citiesManager), "c");
        registerSubCommand("list", new ListCommand(citiesManager), "l");
        registerSubCommand("members", new MembersCommand(citiesManager), "m");
        registerSubCommand("remove", new RemoveCommand(citiesManager));
        registerSubCommand("leave", new LeaveCommand(citiesManager));
        registerSubCommand("redefine", new RedefineCommand(citiesManager));
        registerSubCommand("info", new InfoCommand(citiesManager), "i");
        registerSubCommand("permissions", new PermissionsCommand(citiesManager), "p", "perms");
    }

    public static Tuple3<City, CityPlot, String[]> getTupleAndArgs(String[] args, Player player) {
        var cm = RPMachine.getInstance().getCitiesManager();
        var argNo = 0;

        var currentCity = cm.getCityHere(player.getChunk());
        var providedCity = args.length <= argNo ? null : RPMachine.getInstance().getCitiesManager().getCity(args[argNo]);

        if (providedCity != null) {
            currentCity = providedCity;
            argNo++; // Use the provided city
        } else if (currentCity == null) {
            if (argNo >= args.length) {
                player.sendMessage(ChatColor.RED + "Vous ne vous trouvez dans aucune ville. Merci d'indiquer la ville ciblée.");
            } else {
                player.sendMessage(ChatColor.RED + "Aucune ville du nom de " + ChatColor.DARK_RED + args[argNo] + ChatColor.RED + ". Attention, vous ne vous trouvez dans aucune ville : la saisie du nom de la ville est donc obligatoire.");
            }
            return null;
        }

        // Found the city \o/
        var currentPlot = currentCity.getPlotHere(player.getLocation());
        var providedPlot = args.length <= argNo ? null : currentCity.getPlot(args[argNo]);

        if (providedPlot != null) {
            currentPlot = providedPlot;
            argNo++; // Use the provided plot
        } else if (currentPlot == null) {
            if (argNo >= args.length) {
                player.sendMessage(ChatColor.RED + "Vous ne vous trouvez dans aucune parcelle. Merci d'indiquer la parcelle ciblée.");
            } else {
                player.sendMessage(ChatColor.RED + "Aucune parcelle du nom de " + ChatColor.DARK_RED + args[argNo] + ChatColor.RED + " à " + ChatColor.DARK_RED + currentCity.getCityName() + ChatColor.RED + ". Attention, vous ne vous trouvez dans aucune parcelle : la saisie du nom de la parcelle est donc obligatoire.");
            }
            return null;
        }

        var restOfArgs = Arrays.copyOfRange(args, argNo, args.length);

        return Tuples.of(currentCity, currentPlot, restOfArgs);
    }


}
