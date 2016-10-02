package net.zyuiop.rpmachine.economy.shops;

import net.minecraft.server.v1_8_R2.EntityFireworks;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.economy.EconomyManager;
import net.zyuiop.rpmachine.economy.Messages;
import net.zyuiop.rpmachine.economy.jobs.Job;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftFirework;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

public class ShopSign extends AbstractItemShop {
	private String ownerName;
	private int available;

	public ShopSign() {
		super(ShopSign.class);
	}

	public ShopSign(Location location) {
		super(ShopSign.class, location);
	}

	public int getAvailable() {
		return available;
	}

	public void setAvailable(int available) {
		this.available = available;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
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
				sign.setLine(0, ownerName);
				if (action == ShopAction.BUY) {
					sign.setLine(1, ChatColor.GREEN + "achète "+amountPerPackage);
				} else {
					sign.setLine(1, ChatColor.BLUE + "vend "+amountPerPackage);
				}
				sign.setLine(2, ChatColor.BOLD + itemType.toString());
				sign.setLine(3, ChatColor.BLUE + "Prix : " + price);
			}

			Bukkit.getScheduler().runTask(RPMachine.getInstance(), () -> sign.update());
		} else {
			Bukkit.getLogger().info("Error : sign is not a sign, at "+location.toString());
		}
	}

	public void rightClick(Player player, PlayerInteractEvent event) {
		if (player.getUniqueId().equals(ownerId))
			clickOwner(player, event);
		else
			clickUser(player, event);

		RPMachine.getInstance().getShopsManager().save(this);
	}

	public boolean breakSign(Player player) {
		if (!player.getUniqueId().equals(ownerId)) {
			return false;
		}

		for (; available > 0; available--) {
			Bukkit.getWorld("world").dropItemNaturally(location.getLocation(), new ItemStack(itemType, 1, damage));
		}

		location.getLocation().getBlock().breakNaturally();
		RPMachine.getInstance().getShopsManager().remove(this);
		return true;
	}

	void clickOwner(Player player, PlayerInteractEvent event) {
		if (itemType == null) {
			if (event.getItem() == null)
				return;

			Material type = event.getItem().getType();

			if (action == ShopAction.SELL) {
				Job job = RPMachine.getInstance().getJobsManager().getJob(player.getUniqueId());
				if (job == null) {
					player.sendMessage(ChatColor.RED + "Vous n'avez pas de métier pour le moment.");
					return;
				}

				if (!job.getMaterials().contains(type)) {
					player.sendMessage(ChatColor.RED + "Votre job ne vous permet pas de vendre cela.");
					return;
				}
			}

			itemType = type;
			damage = event.getItem().getDurability();
			player.sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.GREEN + "Votre shop est maintenant totalement oppérationnel.");
			if (action == ShopAction.SELL) {
				player.sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.GREEN + "Cliquez droit avec des items pour les ajouter dans l'inventaire de votre shop.");
			} else {
				player.sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.GREEN + "Cliquez droit pour récupérer le contenu de l'inventaire de votre shop.");
			}
			player.sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.GREEN + "Casser le panneau droppera l'inventaire du shop au sol.");
			display();
		} else {
			if (action == ShopAction.SELL && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (isItemValid(event.getItem())) {
					int amt = event.getItem().getAmount();
					available += amt;
					event.getPlayer().setItemInHand(new ItemStack(Material.AIR, 1));
					event.getPlayer().sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.GREEN + "Vous venez d'ajouter "+ChatColor.AQUA + amt + ChatColor.GREEN + " ressources à votre shop. Il y en a maintenant " + ChatColor.AQUA + available + ChatColor.GREEN + " au total.");
				} else {
					event.getPlayer().sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.YELLOW + "Il y a actuellement " + ChatColor.GOLD + this.available + ChatColor.YELLOW + " items dans la réserve de ce shop.");
				}
			} else {
				if (player.getInventory().firstEmpty() == -1) {
					player.sendMessage(ChatColor.RED + "Vous n'avez pas assez de place dans votre inventaire.");
				} else {
					if (available >= amountPerPackage) {
						player.getInventory().addItem(getNewStack());
						available -= amountPerPackage;
						event.getPlayer().sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.GREEN + "Vous venez de récupérer "+ChatColor.AQUA + amountPerPackage + ChatColor.GREEN + " ressources à votre shop. Il en reste " + ChatColor.AQUA + available + ChatColor.GREEN + ".");
					} else {
						player.sendMessage(Messages.SHOPS_PREFIX.getMessage() +  ChatColor.RED + "Il n'y a aucun item à récupérer.");
					}
				}
			}
		}
	}

	public Material getItemType() {
		return itemType;
	}

	public void setItemType(Material itemType) {
		this.itemType = itemType;
	}

	public int getAmountPerPackage() {
		return amountPerPackage;
	}

	public void setAmountPerPackage(int amountPerPackage) {
		this.amountPerPackage = amountPerPackage;
	}

	public ShopAction getAction() {
		return action;
	}

	public void setAction(ShopAction action) {
		this.action = action;
	}

	void clickUser(Player player, PlayerInteractEvent event) {
		if (itemType == null) {
			player.sendMessage(ChatColor.RED + "Le créateur de ce shop n'a pas terminé sa configuration.");
		} else if (action == ShopAction.BUY) {
			ItemStack click = event.getItem();
			if (isItemValid(click) && click.getAmount() >= amountPerPackage) {
				EconomyManager manager = RPMachine.getInstance().getEconomyManager();
				manager.transferMoneyBalanceCheck(this.getOwnerId(), player.getUniqueId(), price, result -> {
					if (result) {
						available += amountPerPackage;
						player.sendMessage(Messages.RECEIVED_MONEY.getMessage().replace("{AMT}", "" + price).replace("{FROM}", ownerName));
						click.setAmount(click.getAmount() - amountPerPackage);
						player.getInventory().setItemInHand(click);
					} else {
						player.sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.RED + "L'acheteur n'a plus assez d'argent pour cela.");
					}
				});
			} else {
				player.sendMessage(ChatColor.RED + "Vous devez cliquer sur la panneau en tenant " + ChatColor.AQUA + itemType.toString() + ChatColor.RED + " en main.");
			}
		} else if (action == ShopAction.SELL) {
			if (available < amountPerPackage) {
				player.sendMessage(ChatColor.RED + "Il n'y a pas assez d'items à vendre.");
				return;
			} else if (player.getInventory().firstEmpty() == -1) {
				player.sendMessage(ChatColor.RED + "Vous n'avez pas assez de place dans votre inventaire.");
				return;
			}

			EconomyManager manager = RPMachine.getInstance().getEconomyManager();
			manager.transferMoneyBalanceCheck(player.getUniqueId(), getOwnerId(), price, result -> {
				if (result) {
					player.sendMessage(Messages.SHOPS_PREFIX.getMessage() + ChatColor.GREEN + "Vous avez bien acheté " + amountPerPackage + itemType.toString() + " pour " + price + " " + EconomyManager.getMoneyName());
					available -= amountPerPackage;
					player.getInventory().addItem(getNewStack());
				} else {
					player.sendMessage(Messages.NOT_ENOUGH_MONEY.getMessage());
				}
			});
		}
	}



	public static void launchfw(final Location loc, final FireworkEffect effect) {
			final Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
			FireworkMeta fwm = fw.getFireworkMeta();
			fwm.addEffect(effect);
			fwm.setPower(0);
			fw.setFireworkMeta(fwm);
			((CraftFirework)fw).getHandle().setInvisible(true);
			Bukkit.getScheduler().runTaskLater(RPMachine.getInstance(), (Runnable) () -> {
				net.minecraft.server.v1_8_R2.World w = (((CraftWorld) loc.getWorld()).getHandle());
				EntityFireworks fireworks = ((CraftFirework)fw).getHandle();
				w.broadcastEntityEffect(fireworks, (byte)17);
				fireworks.die();
			}, 5);
		}


}
