package net.zyuiop.rpmachine.economy.listeners;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.economy.TaxPayerToken;
import net.zyuiop.rpmachine.economy.shops.AbstractShopSign;
import net.zyuiop.rpmachine.economy.shops.ItemShopSign;
import net.zyuiop.rpmachine.economy.shops.PlotSign;
import net.zyuiop.rpmachine.economy.shops.ShopAction;
import net.zyuiop.rpmachine.permissions.ShopPermissions;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignsListener implements Listener {

	private final RPMachine plugin;

	public SignsListener(RPMachine plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onSignPlace(SignChangeEvent event) {
		if (!event.getPlayer().getWorld().getName().equals("world"))
			return;

		TaxPayerToken tt = RPMachine.getPlayerRoleToken(event.getPlayer());

		String line0 = event.getLine(0);
		if (line0 == null || line0.equalsIgnoreCase(""))
			line0 = ((Sign) event.getBlock().getState()).getLine(0);

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

					ItemShopSign sign = new ItemShopSign(event.getBlock().getLocation());

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
						if (!tt.checkDelegatedPermission(event.getPlayer(), ShopPermissions.CREATE_BUY_SHOPS))
							return;
						sign.setAction(ShopAction.BUY);
					} else if (action.equalsIgnoreCase("vente")) {
						if (!tt.checkDelegatedPermission(event.getPlayer(), ShopPermissions.CREATE_SELL_SHOPS))
							return;
						sign.setAction(ShopAction.SELL);
					} else {
						showSignsRules(event.getPlayer());
					}
					sign.setOwner(tt);
					event.getPlayer().sendMessage(ChatColor.AQUA + "[" + ChatColor.GREEN + "Shops" + ChatColor.AQUA + "] " + ChatColor.GREEN + "Votre boutique est presque prête à l'emploi. Cliquez droit avec l'item que vous souhaitez vendre pour terminer la configuration.");

					plugin.getShopsManager().create(sign);
				} catch (Exception e) {
					showSignsRules(event.getPlayer());
				}
			}
		} else if (event.getLine(0).equalsIgnoreCase("PlotShop")) {
			String price = event.getLine(1);
			String restrict = event.getLine(2);
			Plot plot;

			if (!tt.checkDelegatedPermission(event.getPlayer(), ShopPermissions.CREATE_PLOT_SHOPS))
				return;

			City city = RPMachine.getInstance().getCitiesManager().getCityHere(event.getBlock().getChunk());
			if (city != null) {
				plot = city.getPlotHere(event.getBlock().getLocation());
				if (plot == null) {
					event.getPlayer().sendMessage(ChatColor.RED + "Votre panneau ne se trouve pas dans une parcelle.");
					event.getBlock().breakNaturally();
					return;
				}
			} else {
				event.getPlayer().sendMessage(ChatColor.RED + "Votre panneau ne se trouve pas dans une ville.");
				event.getBlock().breakNaturally();
				return;
			}

			if (price == null || restrict == null) {
				showPlotSignsRules(event.getPlayer());
				event.getBlock().breakNaturally();
			} else {
				try {
					Double dprice = Double.valueOf(price);

					TaxPayerToken token = RPMachine.getPlayerRoleToken(event.getPlayer());
					if (!plot.getOwner().equals(token)) {
						if (token.getCityName() == null || !token.getCityName().equals(city.getCityName())) { // pas une ville
							event.getPlayer().sendMessage(ChatColor.RED + "Vous n'êtes pas propriétaire de cette parcelle.");
							event.getBlock().breakNaturally();
							return;
						}
					}

					if (dprice > 9_999_999) {
						event.getPlayer().sendMessage(ChatColor.RED + "Le prix entré est trop grand.");
						return;
					}

					boolean citizensOnly = restrict.equalsIgnoreCase("citizens");
					PlotSign sign = new PlotSign(event.getBlock().getLocation(), plot.getPlotName(), citizensOnly, city.getCityName());
					sign.setPrice(dprice);
					sign.setOwner(token);

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
		player.sendMessage(ChatColor.YELLOW + "- <all|citizens> : définit si la parcelle peut être achetée par tous (all) ou par les citoyens de la ville uniquement (citizens)");
		player.sendMessage(ChatColor.YELLOW + "Votre panneau doit obligatoirement se trouver dans la parcelle à vendre.");
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
			if (sign instanceof ItemShopSign) {
				ItemShopSign ssign = (ItemShopSign) sign;
				p.sendMessage(ChatColor.YELLOW + "Action : " + ssign.getAction());
				p.sendMessage(ChatColor.YELLOW + "Item : " + ssign.getItemType());
				p.sendMessage(ChatColor.YELLOW + "Amount per package : " + ssign.getAmountPerPackage());
				p.sendMessage(ChatColor.YELLOW + "Owner (Name/UUID) : " + sign.getOwner().displayable());
				p.sendMessage(ChatColor.YELLOW + "Available items : " + ((ItemShopSign) sign).getAvailable());
			} else {
				PlotSign psign = (PlotSign) sign;
				p.sendMessage(ChatColor.YELLOW + "Parcelle : " + psign.getPlotName());
				p.sendMessage(ChatColor.YELLOW + "Ville : " + psign.getCityName());
				p.sendMessage(ChatColor.YELLOW + "Citizens Only : " + psign.isCitizensOnly());
				p.sendMessage(ChatColor.YELLOW + "Owner (Name/UUID) : " + sign.getOwner().displayable());
			}
		} else
			sign.rightClick(event.getPlayer(), event);
	}

	@EventHandler
	public void onBreak(BlockBreakEvent event) {
		if (event.getBlock().getBlockData() instanceof Sign) {
			event.setCancelled(true);
			AbstractShopSign sign = plugin.getShopsManager().get(event.getBlock().getLocation());
			if (sign == null)
				event.setCancelled(false);
			else
				sign.breakSign(event.getPlayer());
		} else {
			for (BlockFace face : BlockFace.values()) {
				Block block = event.getBlock().getRelative(face);
				if (block.isEmpty())
					continue;

				if (event.getBlock().getBlockData() instanceof Sign) {
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
