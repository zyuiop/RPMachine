package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.cities.Line;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.entity.Player;

public class BordersCommand implements CityMemberSubCommand {
    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "affiche les bordures de la ville";
    }

    @Override
    public boolean requiresCouncilPrivilege() {
        return true;
    }

    @Override
    public boolean run(Player player, City city, String command, String subcommand, String[] args) {
        for (Line l : city.getBorders())
            l.display(player);
        return true;
    }
}
