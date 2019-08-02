package net.zyuiop.rpmachine.projects.subcommands;

import net.zyuiop.rpmachine.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class WandCommand implements SubCommand {
    @Override
    public String getUsage() {
        return "";
    }

    @Override
    public String getDescription() {
        return "obtient l'outil de sélection de parcelles";
    }

    @Override
    public boolean canUse(Player player) {
        return player.hasPermission("zones.select");
    }

    @Override
    public boolean run(Player player, String command, String subCommand, String[] args) {
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) {
            player.sendMessage(ChatColor.RED + "Vous avez déjà un item en main.");
        } else {
            ItemStack item = new ItemStack(Material.BLAZE_ROD, 1);
            List<String> lores = new ArrayList<>();
            lores.add("Permet de délimiter une zone de projet");
            lores.add("Clic gauche pour le premier point");
            lores.add("Clic droit pour le second point");
            lores.add("Pour de l'aide : " + ChatColor.GREEN + "/projet help");
            ItemMeta im = item.getItemMeta();
            im.setLore(lores);
            im.setDisplayName(ChatColor.RED + "Outil de Zones");
            item.setItemMeta(im);

            player.getInventory().setItemInMainHand(item);
            player.sendMessage(ChatColor.RED + "Vous avez désormais l'Outil de Zones en main.");
            player.sendMessage(ChatColor.RED + "Sélectionnez une région cuboidale en utilisant clic droit et clic gauche.");
        }
        return true;
    }
}
