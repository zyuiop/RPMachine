package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MembersCommand implements CityMemberSubCommand {
    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "affiche les membres de la ville";
    }

    @Override
    public boolean run(Player player, City city, String[] args) {
        player.sendMessage(ChatColor.GOLD + "-----[ Liste des Habitants ]-----");
        for (UUID inhabitant : city.getInhabitants()) {
            String name = RPMachine.database().getUUIDTranslator().getName(inhabitant);
            if (name != null)
                player.sendMessage(ChatColor.YELLOW + " - " + name);
        }

        return true;
    }
}
