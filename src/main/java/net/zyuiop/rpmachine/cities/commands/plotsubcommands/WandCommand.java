package net.zyuiop.rpmachine.cities.commands.plotsubcommands;

import net.zyuiop.rpmachine.cities.CitiesManager;
import net.zyuiop.rpmachine.cities.commands.SubCommand;
import net.zyuiop.rpmachine.cities.data.City;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class WandCommand implements SubCommand {
	private final CitiesManager citiesManager;

	public WandCommand(CitiesManager citiesManager) {
		this.citiesManager = citiesManager;
	}

	@Override
	public String getUsage() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Vous donne l'objet de sélection de zones de parcelles.";
	}

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			City city = citiesManager.getPlayerCity(player.getUniqueId());
			if (city == null) {
				player.sendMessage(ChatColor.RED + "Vous n'avez pas de ville.");
			} else if (!city.getCouncils().contains(player.getUniqueId()) && !player.getUniqueId().equals(city.getMayor())) {
				player.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de définir des parcelles dans votre ville.");
			} else if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR)  {
				player.sendMessage(ChatColor.RED + "Vous avez déjà un item en main.");
			} else {
				ItemStack item = new ItemStack(Material.STICK, 1);
				List<String> lores = new ArrayList<String>();
				lores.add("Permet de délimiter une parcelle");
				lores.add("Clic gauche pour le premier point");
				lores.add("Clic droit pour le second point");
				lores.add("Pour de l'aide : "+ChatColor.GREEN+"/parcelle help");
				ItemMeta im = item.getItemMeta();
				im.setLore(lores);
				im.setDisplayName(ChatColor.GOLD+"Outil de Parcelles");
				item.setItemMeta(im);

				player.setItemInHand(item);
				player.sendMessage(ChatColor.GOLD + "Vous avez désormais l'Outil de Parcelles en main.");
				player.sendMessage(ChatColor.GOLD + "Sélectionnez une région cuboidale en utilisant clic droit et clic gauche.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Commande réservée aux joueurs.");
		}
	}
}
