package net.zyuiop.rpmachine.economy.shops;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.economy.EconomyManager;
import net.zyuiop.rpmachine.economy.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class AdminShopSign extends AbstractItemShop {
	public AdminShopSign() {
		super(AdminShopSign.class);
	}

	public AdminShopSign(Location location) {
		super(AdminShopSign.class, location);
	}

	public void display() {
		Block block = location.getLocation().getBlock();
		if (block.getState() instanceof Sign) {
			Sign sign = (Sign) block.getState();
			if (itemType == null) {
				sign.setLine(0, ChatColor.AQUA + "Boutique");
				sign.setLine(1, ChatColor.RED + "Non configuré");
				sign.setLine(3, ChatColor.RED + "<CLIC DROIT>");
			} else {
				sign.setLine(0, ChatColor.RED + "La Communauté");
				if (action == ShopAction.BUY) {
					sign.setLine(1, ChatColor.GREEN + "achète " + amountPerPackage);
				} else {
					sign.setLine(1, ChatColor.BLUE + "vend " + amountPerPackage);
				}
				sign.setLine(2, ChatColor.BOLD + itemType.toString());
				sign.setLine(3, ChatColor.BLUE + "Prix : " + price);
			}

			Bukkit.getScheduler().runTask(RPMachine.getInstance(), () -> sign.update());
		} else {
			Bukkit.getLogger().info("Error : sign is not a sign, at " + location.toString());
		}
	}

	public void rightClick(Player player, PlayerInteractEvent event) {
		if (player.getUniqueId().equals(ownerId) && itemType == null) {
			clickOwner(player, event);
		} else { clickUser(player, event); }

		RPMachine.getInstance().getShopsManager().save(this);
	}

	public boolean breakSign(Player player) {
		if (!player.getUniqueId().equals(ownerId)) {
			return false;
		}

		location.getLocation().getBlock().setType(Material.AIR);
		RPMachine.getInstance().getShopsManager().remove(this);
		return true;
	}

	void clickOwner(Player player, PlayerInteractEvent event) {
		if (itemType == null) {
			if (event.getItem() == null) { return; }

			itemType = event.getItem().getType();
			player.sendMessage(ChatColor.AQUA + "[" + ChatColor.GREEN + "Shops" + ChatColor.AQUA + "] " + ChatColor.GREEN + "Votre shop est maintenant totalement oppérationnel.");
			display();
		}
	}

	void clickUser(Player player, PlayerInteractEvent event) {
		if (itemType == null) {
			player.sendMessage(ChatColor.RED + "Le créateur de ce shop n'a pas terminé sa configuration.");
		} else if (action == ShopAction.BUY) {
			ItemStack click = event.getItem();
			if (isItemValid(click) && click.getAmount() >= amountPerPackage) {
				EconomyManager manager = RPMachine.getInstance().getEconomyManager();
				manager.giveMoney(player.getUniqueId(), price, (newAmount, difference) -> {
					click.setAmount(click.getAmount() - amountPerPackage);
					event.getPlayer().getInventory().setItemInHand(click);
					player.sendMessage(ChatColor.GREEN + "Vous avez bien vendu " + amountPerPackage + " " + itemType.toString() + " pour " + difference + " " + EconomyManager.getMoneyName());
				});
			} else {
				player.sendMessage(ChatColor.RED + "Vous devez cliquer sur la panneau en tenant " + ChatColor.AQUA + itemType.toString() + ChatColor.RED + " en main.");
			}
		} else if (action == ShopAction.SELL) {
			if (player.getInventory().firstEmpty() == -1) {
				player.sendMessage(ChatColor.RED + "Vous n'avez pas assez de place dans votre inventaire.");
				return;
			}

			EconomyManager manager = RPMachine.getInstance().getEconomyManager();
			manager.withdrawMoneyWithBalanceCheck(player.getUniqueId(), price, (newAmount, difference) -> {
				if (difference != price) {
					player.sendMessage(Messages.NOT_ENOUGH_MONEY.getMessage());
				} else {
					player.getInventory().addItem(this.getNewStack());
					player.sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.GREEN + "Vous avez bien acheté " + amountPerPackage + " " + itemType.toString() + " pour " + price + " " + EconomyManager.getMoneyName());
				}
			});
		}
	}
}
