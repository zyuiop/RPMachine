package net.zyuiop.rpmachine.projects;

import java.util.*;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.LandOwner;
import net.zyuiop.rpmachine.common.Area;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.economy.ShopOwner;
import net.zyuiop.rpmachine.economy.TaxPayer;
import net.zyuiop.rpmachine.permissions.DelegatedPermission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author zyuiop
 *         A zone is a kind of plot located outside of any city. It can only be created with administrative permissions.<br/>
 *         A zone has no taxes as it doesn't depend of any city.
 */
public class Project extends Plot implements TaxPayer, ShopOwner, LandOwner {
	private String welcomeMessage;
	private String goodByeMessage;
	private String fileName;

	private double money = 0D;
	private Map<String, Double> unpaidTaxes = new HashMap<>();
	private Map<String, Date> lastPaidTaxes = new HashMap<>();
	private final Map<UUID, Set<DelegatedPermission>> permissions = new HashMap<>();


	public String getWelcomeMessage() {
		return welcomeMessage;
	}

	public void setWelcomeMessage(String welcomeMessage) {
		this.welcomeMessage = welcomeMessage;
	}

	public String getGoodByeMessage() {
		return goodByeMessage;
	}

	public void setGoodByeMessage(String goodByeMessage) {
		this.goodByeMessage = goodByeMessage;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public boolean checkArea(Area area, ProjectsManager manager, Player player) {
		int i_x = area.getMinX();
		while (i_x < area.getMaxX()) {
			int i_z = area.getMinZ();
			while (i_z < area.getMaxZ()) {
				if (RPMachine.getInstance().getCitiesManager().getCityHere(new Location(Bukkit.getWorld("world"), i_x, 64, i_z).getChunk()) != null) {
					player.sendMessage(ChatColor.RED + "Une partie de votre sélection est dans une ville.");
					return false;
				}

				int i_y = area.getMinY();
				while (i_y < area.getMaxY()) {
					Project check = manager.getZoneHere(new Location(Bukkit.getWorld("world"), i_x, i_y, i_z));
					if (check != null && !check.getPlotName().equals(getPlotName())) {
						player.sendMessage(ChatColor.RED + "Une partie de votre sélection fait partie d'un autre projet");
						return false;
					}
					i_y ++;
				}
				i_z ++;
			}
			i_x ++;
		}

		setArea(area);
		return true;
	}

	public void save() {
		RPMachine.getInstance().getProjectsManager().saveZone(this);
	}

	@Override
	public double getMoney() {
		return money;
	}

	@Override
	public void setMoney(double amount) {
		this.money = amount;
		save();
	}

	@Override
	public boolean withdrawMoney(double amount) {
		if (getMoney() >= amount) {
			setMoney(getMoney() - amount);
			return true;
		}
		return false;
	}

	@Override
	public void creditMoney(double amount) {
		setMoney(getMoney() + amount);
	}

	public void setUnpaidTaxes(Map<String, Double> unpaidTaxes) {
		this.unpaidTaxes = unpaidTaxes;
	}

	public Map<String, Date> getLastPaidTaxes() {
		return lastPaidTaxes;
	}

	public void setLastPaidTaxes(Map<String, Date> lastPaidTaxes) {
		this.lastPaidTaxes = lastPaidTaxes;
	}

	@Override
	public void setUnpaidTaxes(String city, double amount) {
		unpaidTaxes.put(city, amount);
		save();
	}

	@Override
	public double getUnpaidTaxes(String city) {
		return unpaidTaxes.get(city);
	}

	@Override
	public void setLastTaxes(String city, Date date) {
		lastPaidTaxes.put(city, date);
	}

	@Override
	public Date getLastTaxes(String city) {
		return lastPaidTaxes.get(city);
	}

	@Override
	public Map<String, Double> getUnpaidTaxes() {
		return unpaidTaxes;
	}

	@Override
	public boolean hasDelegatedPermission(Player player, DelegatedPermission permission) {
		if (getOwner().hasPermission(player, permission))
			return true;
		else if (permissions.containsKey(player.getUniqueId()))
			return permissions.get(player.getUniqueId()).contains(permission);
		return false;
	}

	@Override
	public boolean canManageShop(Player player) {
		return getOwner().getShopOwner().canManageShop(player);
	}

	@Override
	public boolean canManagePlot(Player player) {
		return getOwner().getLandOwner().canManagePlot(player);
	}
}
