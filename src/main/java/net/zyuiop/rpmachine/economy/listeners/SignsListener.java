package net.zyuiop.rpmachine.economy.listeners;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.Plot;
import net.zyuiop.rpmachine.economy.shops.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignsListener implements Listener {

	private final RPMachine plugin;

	public SignsListener(RPMachine plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSignPlace(BlockPlaceEvent event) {
		if (event.getBlockPlaced().getState() instanceof Sign && event.getPlayer().isSneaking()) {
			City city = RPMachine.getInstance().getCitiesManager().getCityHere(event.getBlock().getChunk());
			if (city != null) {
				Plot plot = city.getPlotHere(event.getBlock().getLocation());
				if (plot != null &&
						(event.getPlayer().getUniqueId().equals(plot.getOwner())
								|| city.getMayor().equals(event.getPlayer().getUniqueId())
								|| (plot.getOwner() == null
								&& city.getCouncils().contains(event.getPlayer().getUniqueId())))) {
					Player player = event.getPlayer();
					player.sendMessage(ChatColor.GOLD + "Votre parcelle actuelle : " + ChatColor.YELLOW + plot.getPlotName());

					/*Sign sign = (Sign) event.getBlockPlaced().getState();
					sign.setLine(0, "PlotShop");
					sign.setLine(2, plot.getPlotName());
					sign.update();

					CraftPlayer player = ((CraftPlayer) event.getPlayer());
					PacketPlayOutOpenSignEditor editor = new PacketPlayOutOpenSignEditor(new BlockPosition(sign.getLocation().getX(), sign.getLocation().getY(), sign.getLocation().getZ()));
					Bukkit.getScheduler().runTaskLater(plugin, () -> player.getHandle().playerConnection.sendPacket(editor), 5);
					*/
				}
			}
		}
	}

	@EventHandler
	public void onSignPlace(SignChangeEvent event) {
		if (!event.getPlayer().getWorld().getName().equals("world"))
			return;

		String line0 = event.getLine(0);
		if (line0 == null || line0.equalsIgnoreCase(""))
			line0 = ((Sign) event.getBlock().getState()).getLine(0);
		String line2 = event.getLine(2);
		if (line2 == null || line2.equalsIgnoreCase(""))
			line2 = ((Sign) event.getBlock().getState()).getLine(2);

		if (line0.equalsIgnoreCase("Shop")) {
			String price = event.getLine(1);
			String bundleSize = event.getLine(2);
			String action = event.getLine(3);

			if (price == null || bundleSize == null || action == null) {
				showSignsRules(event.getPlayer());
				event.getBlock().breakNaturally();
			} else {
				try {
					Double dprice = Double.valueOf(price);
					Integer ibundle = Integer.valueOf(bundleSize);

					ShopSign sign = new ShopSign(event.getBlock().getLocation());

					if (ibundle > 64) {
						event.getPlayer().sendMessage(ChatColor.RED + "Vous ne pouvez pas vendre des lots de plus de 64 items.");
						return;
					} else {
						sign.setAmountPerPackage(ibundle);
					}

					if (dprice > 9999999) {
						event.getPlayer().sendMessage(ChatColor.RED + "Le prix entré est trop grand.");
						return;
					} else {
						sign.setPrice(dprice);
					}

					if (action.equalsIgnoreCase("achat")) {
						sign.setAction(ShopAction.BUY);
					} else if (action.equalsIgnoreCase("vente")) {
						sign.setAction(ShopAction.SELL);
					} else {
						showSignsRules(event.getPlayer());
					}

					sign.setOwnerId(event.getPlayer().getUniqueId());
					sign.setOwnerName(event.getPlayer().getName());

					event.getPlayer().sendMessage(ChatColor.AQUA + "[" + ChatColor.GREEN + "Shops" + ChatColor.AQUA + "] " + ChatColor.GREEN + "Votre boutique est presque prête à l'emploi. Cliquez droit avec l'item que vous souhaitez vendre pour terminer la configuration.");

					plugin.getShopsManager().create(sign);
				} catch (Exception e) {
					showSignsRules(event.getPlayer());
				}
			}
		} else if (event.getLine(0).equalsIgnoreCase("AdminShop") && event.getPlayer().hasPermission("rp.adminshop")) {
			String price = event.getLine(1);
			String bundleSize = event.getLine(2);
			String action = event.getLine(3);

			if (price == null || bundleSize == null || action == null) {
				showSignsRules(event.getPlayer());
			} else {
				try {
					Double dprice = Double.valueOf(price);
					Integer ibundle = Integer.valueOf(bundleSize);

					AdminShopSign sign = new AdminShopSign(event.getBlock().getLocation());

					if (ibundle > 64) {
						event.getPlayer().sendMessage(ChatColor.RED + "Vous ne pouvez pas vendre des lots de plus de 64 items.");
						return;
					} else {
						sign.setAmountPerPackage(ibundle);
					}

					if (dprice > 9999999) {
						event.getPlayer().sendMessage(ChatColor.RED + "Le prix entré est trop grand.");
						return;
					} else {
						sign.setPrice(dprice);
					}

					if (action.equalsIgnoreCase("achat")) {
						sign.setAction(ShopAction.BUY);
					} else if (action.equalsIgnoreCase("vente")) {
						sign.setAction(ShopAction.SELL);
					} else {
						showSignsRules(event.getPlayer());
					}

					sign.setOwnerId(event.getPlayer().getUniqueId());
					event.getPlayer().sendMessage(ChatColor.AQUA + "[" + ChatColor.GREEN + "Shops" + ChatColor.AQUA + "] " + ChatColor.GREEN + "Votre boutique est presque prête à l'emploi. Cliquez droit avec l'item que vous souhaitez vendre pour terminer la configuration.");

					plugin.getShopsManager().create(sign);
				} catch (Exception e) {
					showSignsRules(event.getPlayer());
				}
			}
		} else if (event.getLine(0).equalsIgnoreCase("PlotShop")) {
			String price = event.getLine(1);
			String plotname = line2;
			String restrict = event.getLine(3);

			if (plotname == null) {
				City city = RPMachine.getInstance().getCitiesManager().getCityHere(event.getBlock().getChunk());
				if (city != null) {
					Plot plot = city.getPlotHere(event.getBlock().getLocation());
					if (plot != null &&
							(event.getPlayer().getUniqueId().equals(plot.getOwner())
									|| city.getMayor().equals(event.getPlayer().getUniqueId())
									|| (plot.getOwner() == null
									&& city.getCouncils().contains(event.getPlayer().getUniqueId())))) {
						plotname = plot.getPlotName();
					}
				}
			}

			if (price == null || plotname == null || restrict == null) {
				showPlotSignsRules(event.getPlayer());
				event.getBlock().breakNaturally();
			} else if (!event.getBlock().getWorld().getName().equals("world")) {
				event.getPlayer().sendMessage(ChatColor.RED + "Votre panneau ne se trouve pas dans une ville.");
				event.getBlock().breakNaturally();
			} else {
				try {
					Double dprice = Double.valueOf(price);
					City city = RPMachine.getInstance().getCitiesManager().getCityHere(event.getBlock().getChunk());
					if (city == null) {
						event.getPlayer().sendMessage(ChatColor.RED + "Votre panneau ne se trouve pas dans une ville.");
						event.getBlock().breakNaturally();
						return;
					}

					Plot plot = city.getPlots().get(plotname);
					if (plot == null) {
						event.getPlayer().sendMessage(ChatColor.RED + "Cette parcelle n'existe pas.");
						event.getBlock().breakNaturally();
						return;
					}

					if (!event.getPlayer().getUniqueId().equals(plot.getOwner())) {
						if (!(city.getMayor().equals(event.getPlayer().getUniqueId()) || (plot.getOwner() == null && city.getCouncils().contains(event.getPlayer().getUniqueId())))) {
							event.getPlayer().sendMessage(ChatColor.RED + "Vous n'êtes pas propriétaire de cette parcelle.");
							event.getBlock().breakNaturally();
							return;
						}
					}

					if (dprice > 9999999) {
						event.getPlayer().sendMessage(ChatColor.RED + "Le prix entré est trop grand.");
						return;
					}

					boolean citizensOnly = restrict.equalsIgnoreCase("citizens");
					PlotSign sign = new PlotSign(event.getBlock().getLocation(), plot.getPlotName(), citizensOnly, city.getCityName());
					sign.setOwnerId(event.getPlayer().getUniqueId());
					sign.setPrice(dprice);

					event.getPlayer().sendMessage(ChatColor.AQUA + "[" + ChatColor.GREEN + "Shops" + ChatColor.AQUA + "] " + ChatColor.GREEN + "Votre boutique est prête à l'emploi.");

					plugin.getShopsManager().create(sign);
				} catch (Exception e) {
					showPlotSignsRules(event.getPlayer());
				}
			}
		}
	}

	void showSignsRules(Player player) {
		player.sendMessage(ChatColor.RED + "Merci de respecter les règles de panneaux de shops : ");
		player.sendMessage(ChatColor.YELLOW + "- Shop");
		player.sendMessage(ChatColor.YELLOW + "- <prix d'achat/vente>");
		player.sendMessage(ChatColor.YELLOW + "- <taille des lots>");
		player.sendMessage(ChatColor.YELLOW + "- <achat (pour ACHETER aux joueurs) / vente (pour VENDRE aux joueurs)>");
	}

	void showPlotSignsRules(Player player) {
		player.sendMessage(ChatColor.RED + "Merci de respecter les règles de panneaux de ventes de parcelles : ");
		player.sendMessage(ChatColor.YELLOW + "- PlotShop");
		player.sendMessage(ChatColor.YELLOW + "- <prix de vente> : Prix auquel vous vendez la parcelle. La ville prend 20% de taxes.");
		player.sendMessage(ChatColor.YELLOW + "- <nom de la parcelle> : nom de la parcelle à vendre");
		player.sendMessage(ChatColor.YELLOW + "- <all|citizens> : définit si la parcelle peut être achetée par tous (all) ou par les citoyens de la ville uniquement (citizens)");
	}

	@EventHandler
	public void onSignClick(PlayerInteractEvent event) {
		if (event.getClickedBlock() == null)
			return;
		AbstractShopSign sign = plugin.getShopsManager().get(event.getClickedBlock().getLocation());
		if (sign == null)
			return;
		else if (event.getPlayer().isSneaking() && event.getPlayer().hasPermission("sign.debug")) {
			Player p = event.getPlayer();
			p.sendMessage(ChatColor.YELLOW + "-----[ Débug Shop ] -----");
			p.sendMessage(ChatColor.YELLOW + "Price : " + sign.getPrice());
			if (sign instanceof ShopSign) {
				ShopSign ssign = (ShopSign) sign;
				p.sendMessage(ChatColor.YELLOW + "Action : " + ssign.getAction());
				p.sendMessage(ChatColor.YELLOW + "Item : " + ssign.getItemType());
				p.sendMessage(ChatColor.YELLOW + "Amount per package : " + ssign.getAmountPerPackage());
				p.sendMessage(ChatColor.YELLOW + "Owner (Name/UUID) : " + ((ShopSign) sign).getOwnerName() + " / " + sign.getOwnerId());
				p.sendMessage(ChatColor.YELLOW + "Available items : " + ((ShopSign) sign).getAvailable());
			} else if (sign instanceof AdminShopSign) {
				AdminShopSign asign = (AdminShopSign) sign;
				p.sendMessage(ChatColor.YELLOW + "Action : " + asign.getAction());
				p.sendMessage(ChatColor.YELLOW + "Item : " + asign.getItemType());
				p.sendMessage(ChatColor.YELLOW + "Amount per package : " + asign.getAmountPerPackage());
				p.sendMessage(ChatColor.YELLOW + "Admin Shop");
			} else {
				PlotSign psign = (PlotSign) sign;
				p.sendMessage(ChatColor.YELLOW + "Parcelle : " + psign.getPlotName());
				p.sendMessage(ChatColor.YELLOW + "Ville : " + psign.getCityName());
				p.sendMessage(ChatColor.YELLOW + "Citizens Only : " + psign.isCitizensOnly());
			}
		} else
			sign.rightClick(event.getPlayer(), event);
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if (event.getBlock().getType() == Material.SIGN || event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.WALL_SIGN) {
			event.setCancelled(true);
			AbstractShopSign sign = plugin.getShopsManager().get(event.getBlock().getLocation());
			if (sign == null)
				event.setCancelled(false);
			else
				sign.breakSign(event.getPlayer());
		} else {
			for (BlockFace face : BlockFace.values()) {
				Block block = event.getBlock().getRelative(face);
				if (block == null)
					continue;

				if (block.getType() == Material.SIGN || block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
					event.setCancelled(true);
					AbstractShopSign sign = plugin.getShopsManager().get(event.getBlock().getLocation());
					if (sign == null)
						event.setCancelled(false);
					else
						sign.breakSign(event.getPlayer());
				}
			}
		}
	}
}
