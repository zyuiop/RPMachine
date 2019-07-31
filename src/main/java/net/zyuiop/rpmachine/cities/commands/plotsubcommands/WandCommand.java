package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.cities.commands.CityMemberSubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class WandCommand implements CityMemberSubCommand {
    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "récupère l'outil de sélection de zones";
    }

    @Override
    public boolean requiresCouncilPrivilege() {
        return true;
    }

    @Override
    public boolean run(Player player, @Nonnull City city, String[] args) {
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            player.sendMessage(ChatColor.RED + "Vous avez déjà un item en main.");
        } else {
            ItemStack item = new ItemStack(Material.STICK, 1);
            List<String> lores = new ArrayList<String>();
            lores.add("Permet de délimiter une parcelle");
            lores.add("Clic gauche pour le premier point");
            lores.add("Clic droit pour le second point");
            lores.add("Pour de l'aide : " + ChatColor.GREEN + "/parcelle help");
            ItemMeta im = item.getItemMeta();
            im.setLore(lores);
            im.setDisplayName(ChatColor.GOLD + "Outil de Parcelles");
            item.setItemMeta(im);

            player.getInventory().setItemInMainHand(item);
            player.sendMessage(ChatColor.GOLD + "Vous avez désormais l'Outil de Parcelles en main.");
            player.sendMessage(ChatColor.GOLD + "Sélectionnez une région cuboidale en utilisant clic droit et clic gauche.");
        }

        return true;
    }
}
