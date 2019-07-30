package net.zyuiop.rpmachine.economy.shops;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.economy.EconomyManager;
import net.zyuiop.rpmachine.economy.Messages;
import net.zyuiop.rpmachine.economy.TaxPayer;
import net.zyuiop.rpmachine.reflection.ReflectionUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.concurrent.CopyOnWriteArrayList;


public class PlotSign extends AbstractShopSign {
	protected String plotName;
	protected String cityName;
	protected boolean citizensOnly;

	public PlotSign() {
		super();
	}

	public PlotSign(Location location, String regionName, boolean citizensOnly, String cityName) {
		super(location);
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
			if (citizensOnly) {
				sign.setLine(2, ChatColor.RED + "Citoyens");
			} else {
				sign.setLine(2, ChatColor.GREEN + "Public");
			}
			sign.setLine(3, ChatColor.GOLD + "> Acheter <");

			Bukkit.getScheduler().runTask(RPMachine.getInstance(), (Runnable) sign::update);
		} else {
			Bukkit.getLogger().info("Error : sign is not a sign, at " + location.toString());
		}
	}

	public void rightClick(Player player, PlayerInteractEvent event) {
		clickUser(player, event);
		RPMachine.getInstance().getShopsManager().save(this);
	}

	@Override
	protected void doBreakSign(Player ignored) {
		location.getLocation().getBlock().breakNaturally();
		RPMachine.getInstance().getShopsManager().remove(this);
	}

	@Override
	public void breakSign() {
		location.getLocation().getBlock().breakNaturally();
		RPMachine.getInstance().getShopsManager().remove(this);
	}

	void clickOwner(Player player, PlayerInteractEvent event) {
	}

	void clickUser(Player player, PlayerInteractEvent event) {
		if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
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

			player.sendMessage(ChatColor.GOLD + "-----[ Informations Parcelle ]-----");
			player.sendMessage(ChatColor.YELLOW + "Nom : " + plot.getPlotName());
			player.sendMessage(ChatColor.YELLOW + "Ville : " + city.getCityName());
			player.sendMessage(ChatColor.YELLOW + "Surface : " + plot.getArea().getSquareArea() + " blocs²");
			player.sendMessage(ChatColor.YELLOW + "Volume : " + plot.getArea().getVolume() + " blocs³");
			player.sendMessage(ChatColor.YELLOW + "Impots : " + plot.getArea().getSquareArea() * city.getTaxes() + " $");

			return;
		}

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

		if (price < 0) {
			price *= -1;
		}

		TaxPayer data = RPMachine.getPlayerRoleToken(player).getTaxPayer();
		manager.withdrawMoneyWithBalanceCheck(data, price, (newAmount, difference) -> {
			if (difference == 0) {
				player.sendMessage(Messages.NOT_ENOUGH_MONEY.getMessage());
			} else {
				if (getOwner().getCityName() == null) {
					// Pas une ville, on check.
					if (plot.getOwner() == null || !plot.getOwner().equals(getOwner())) {
						// Un joueur peut pas vendre une parcelle random
						breakSign(null);
						player.sendMessage(ChatColor.RED + "Erreur : cette parcelle n'appartient plus à " + getOwner().shortDisplayable());
						Bukkit.getLogger().info("Old plot sign found : " + plot.getOwner() + " / sign " + getOwner());
						return;
					}
				}

				if (plot.getOwner() == null) {
					city.setMoney(city.getMoney() + price);
				} else {
					// On crédite à l'owner du panneau
					getOwner().getTaxPayer().creditMoney(price * 0.8D);
					city.creditMoney(price * 0.2D);
				}

				plot.setOwner(data);
				plot.setPlotMembers(new CopyOnWriteArrayList<>());

				city.getPlots().put(plotName, plot);
				RPMachine.getInstance().getCitiesManager().saveCity(city);
				Bukkit.getScheduler().runTask(RPMachine.getInstance(), () -> {
					doBreakSign(null);
					launchfw(location.getLocation(), FireworkEffect.builder().withColor(Color.WHITE, Color.GRAY, Color.BLACK).with(FireworkEffect.Type.STAR).build());
				});
				player.sendMessage(ChatColor.GREEN + "Vous êtes désormais propriétaire de cette parcelle.");
			}
		});
	}


	public static void launchfw(final Location loc, final FireworkEffect effect) {
		ReflectionUtils.getVersion().launchfw(loc, effect);
	}
}
