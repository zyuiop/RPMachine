package net.zyuiop.rpmachine.economy.shops;

import net.minecraft.server.v1_8_R2.EntityFireworks;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.Plot;
import net.zyuiop.rpmachine.economy.EconomyManager;
import net.zyuiop.rpmachine.economy.Messages;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R2.entity.CraftFirework;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.concurrent.CopyOnWriteArrayList;

public class PlotSign extends AbstractShopSign {
	protected String plotName;
	protected String cityName;
	protected boolean citizensOnly;

	public PlotSign() {
		super(PlotSign.class);
	}

	public PlotSign(Location location, String regionName, boolean citizensOnly, String cityName) {
		super(PlotSign.class, location);
		this.plotName = regionName;
		this.citizensOnly = citizensOnly;
		this.cityName = cityName;
	}

	public String getPlotName() {
		return plotName;
	}

	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public boolean isCitizensOnly() {
		return citizensOnly;
	}

	public void setCitizensOnly(boolean citizensOnly) {
		this.citizensOnly = citizensOnly;
	}

	public void display() {
		Block block = location.getLocation().getBlock();
		if (block.getState() instanceof Sign) {
			Sign sign = (Sign) block.getState();
			sign.setLine(0, ChatColor.GREEN + "[Terrain]");
			sign.setLine(1, ChatColor.BLUE + "Prix : " + price);
			if (citizensOnly)
				sign.setLine(2, ChatColor.RED + "Citoyens");
			else
				sign.setLine(2, ChatColor.GREEN + "Public");
			sign.setLine(3, ChatColor.GOLD + "> Acheter <");

			Bukkit.getScheduler().runTask(RPMachine.getInstance(), sign::update);
		} else {
			Bukkit.getLogger().info("Error : sign is not a sign, at "+location.toString());
		}
	}

	public void rightClick(Player player, PlayerInteractEvent event) {
		clickUser(player, event);
		RPMachine.getInstance().getShopsManager().save(this);
	}

	public boolean breakSign(Player player) {
		if (!player.getUniqueId().equals(ownerId)) {
			return false;
		}

		breakSign();
		return true;
	}

	public void breakSign() {
		location.getLocation().getBlock().breakNaturally();
		RPMachine.getInstance().getShopsManager().remove(this);
	}

	void clickOwner(Player player, PlayerInteractEvent event) {
	}

	void clickUser(Player player, PlayerInteractEvent event) {
		EconomyManager manager = RPMachine.getInstance().getEconomyManager();
		City city = RPMachine.getInstance().getCitiesManager().getCity(cityName);
		if (city == null) {
			player.sendMessage(ChatColor.RED + "Une erreur s'est produite : la ville n'existe pas.");
			return;
		}

		Plot plot = city.getPlots().get(plotName);
		if (plot == null) {
			player.sendMessage(ChatColor.RED + "Une erreur s'est produite : la parcelle n'existe pas.");
			return;
		}

		if (citizensOnly && !city.getInhabitants().contains(player.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "Vous n'êtes pas citoyen de cette ville.");
			return;
		}

		if (price < 0)
			price *= -1;

		manager.withdrawMoneyWithBalanceCheck(player.getUniqueId(), price, (newAmount, difference, error) -> {
			if (difference == 0) {
				player.sendMessage(Messages.NOT_ENOUGH_MONEY.getMessage());
			} else {
				if (plot.getOwner() == null) {
					city.setMoney(city.getMoney() + price);
				} else {
					manager.giveMoney(plot.getOwner(), price * 0.8);
					city.setMoney(city.getMoney() + (price*0.2));
				}

				plot.setOwner(player.getUniqueId());
				plot.setPlotMembers(new CopyOnWriteArrayList<>());

				city.getPlots().put(plotName, plot);
				RPMachine.getInstance().getCitiesManager().saveCity(city);
				Bukkit.getScheduler().runTask(RPMachine.getInstance(), () -> {
					breakSign();
					launchfw(location.getLocation(), FireworkEffect.builder().withColor(Color.WHITE, Color.GRAY, Color.BLACK).with(FireworkEffect.Type.STAR).build());
				});
				player.sendMessage(ChatColor.GREEN + "Vous êtes désormais propriétaire de cette parcelle.");
			}
		});
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
