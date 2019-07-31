package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.common.Plot;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class ListCommand implements CityMemberSubCommand {
    private final CitiesManager citiesManager;

    public ListCommand(CitiesManager citiesManager) {
        this.citiesManager = citiesManager;
    }

    @Override
    public String getUsage() {
        return "[empty|claimed]";
    }

    @Override
    public String getDescription() {
        return "liste les parcelles dans votre ville";
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String[] args) {
        boolean claimed = true;
        boolean empty = true;
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("empty"))
                claimed = false;
            else if (args[0].equalsIgnoreCase("claimed"))
                empty = false;
        }

        player.sendMessage(ChatColor.YELLOW + "-----[ Liste des Parcelles ]-----");
        for (Plot plot : city.getPlots().values()) {
            if (plot.getOwner() == null && empty)
                player.sendMessage(ChatColor.YELLOW + " - " + plot.getPlotName() + ", " + ChatColor.RED + "Aucun proprio.");
            else if (claimed) {
                String prop = plot.getOwner().displayable();
                player.sendMessage(ChatColor.YELLOW + " - " + plot.getPlotName() + ", " + ChatColor.GREEN + ((prop == null) ? "Proprio inconnu" : "Proprio : " + prop));
            }
        }

        return true;
    }
}
