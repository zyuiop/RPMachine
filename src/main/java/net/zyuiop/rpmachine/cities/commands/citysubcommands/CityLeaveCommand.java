package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.City;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class CityLeaveCommand implements CityMemberSubCommand {
    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "quitte la ville dont vous êtes citoyen.";
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String command, String subcommand, String[] args) {
        if (city.getMayor().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Vous ne pouvez quitter une ville dont vous êtes le maire. Si vous souhaitez supprimer votre ville, contactez le staff. Sinon, nommez un autre maire avec /city setmayor <pseudo>");
            return false;
        } else {
            if (city.getCouncils().contains(player.getUniqueId()))
                city.getCouncils().remove(player.getUniqueId());

            city.getInhabitants().remove(player.getUniqueId());
            city.getInvitedUsers().remove(player.getUniqueId());
            RPMachine.getInstance().getCitiesManager().saveCity(city);

            player.sendMessage(ChatColor.RED + "Vous n'êtes plus citoyen de " + city.getCityName());
            return true;
        }
    }
}
