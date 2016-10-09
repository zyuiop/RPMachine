package net.zyuiop.rpmachine.zones.subcommands;

import net.zyuiop.rpmachine.cities.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
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
		return "Vous donne l'objet de sélection de zones.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;

			if (!player.hasPermission("zones.select")) {
				player.sendMessage(ChatColor.RED + "Vous n'avez pas la permission.");
				return;
			}

			if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
				player.sendMessage(ChatColor.RED + "Vous avez déjà un item en main.");
			} else {
				ItemStack item = new ItemStack(Material.BLAZE_ROD, 1);
				List<String> lores = new ArrayList<>();
				lores.add("Permet de délimiter une zone");
				lores.add("Clic gauche pour le premier point");
				lores.add("Clic droit pour le second point");
				lores.add("Pour de l'aide : " + ChatColor.GREEN + "/zone help");
				ItemMeta im = item.getItemMeta();
				im.setLore(lores);
				im.setDisplayName(ChatColor.RED + "Outil de Zones");
				item.setItemMeta(im);

				player.setItemInHand(item);
				player.sendMessage(ChatColor.RED + "Vous avez désormais l'Outil de Zones en main.");
				player.sendMessage(ChatColor.RED + "Sélectionnez une région cuboidale en utilisant clic droit et clic gauche.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
