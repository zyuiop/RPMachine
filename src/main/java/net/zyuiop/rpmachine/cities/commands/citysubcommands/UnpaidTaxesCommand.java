package net.zyuiop.rpmachine.cities.commands.citysubcommands;

import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Map;

public class UnpaidTaxesCommand implements CityMemberSubCommand {
    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "affiche les impôts impayés";
    }

    @Override
    public Permission requiresPermission() {
        return CityPermissions.CHECK_TAXES;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String command, String subcommand, String[] args) {
        player.sendMessage(ChatColor.GOLD + "-----[ Impôts impayés ]-----");
        for (Map.Entry<String, Double> entry : city.getTaxesToPay().entrySet()) {
            String name = LegalEntity.getEntity(entry.getKey()).displayable();
            player.sendMessage(ChatColor.YELLOW + " - " + (name == null ? "§cInconnu§e" : name + "§e") + " doit " + ChatColor.RED + entry.getValue() + ChatColor.YELLOW + " à la ville.");
        }

        return true;
    }
}
