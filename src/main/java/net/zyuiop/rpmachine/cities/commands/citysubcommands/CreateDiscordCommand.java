package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

/**
 * @author Louis Vialar
 */
public class CreateDiscordCommand implements CityMemberSubCommand {
    @Override
    public boolean requiresMayorPrivilege() {
        return true;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String command, String subCommand, String[] args) {
        if (city.getPublicChannelId() != 0) {
            player.sendMessage(ChatColor.RED + "Votre ville a déjà des canaux discord.");
            player.sendMessage(ChatColor.GRAY + "Les permissions se mettent à jour toutes les 5 minutes.");
            return true;
        }

        if (city.countInhabitants() < 8) {
            player.sendMessage(ChatColor.RED + "Il faut au moins 8 habitants pour créer des canaux discord.");
            return true;
        }

        RPMachine.getInstance().getDiscordManager().createChannels(city);
        player.sendMessage(ChatColor.GREEN + "Parfait ! Vos canaux ont été créés :)");
        return true;
    }

    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "crée des canaux discord pour votre ville";
    }
}
