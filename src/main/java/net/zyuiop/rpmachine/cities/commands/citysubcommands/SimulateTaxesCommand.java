package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.economy.EconomyManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class SimulateTaxesCommand implements CityMemberSubCommand {
    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "affiche les bénéfices prévus des taxes";
    }


    @Override
    public boolean requiresCouncilPrivilege() {
        return true;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String[] args) {
        player.sendMessage(ChatColor.GOLD + "-----[ Simulation d'impôts ]-----");
        player.sendMessage(ChatColor.YELLOW + "Les impôts actuels de votre ville vous rapportent " + ChatColor.GREEN + "" + city.simulateTaxes() + " " + EconomyManager.getMoneyName() + " par semaine.");

        return true;
    }
}
