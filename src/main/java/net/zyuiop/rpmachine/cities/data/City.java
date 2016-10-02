package net.zyuiop.rpmachine.cities.data;

import net.bridgesapi.api.BukkitBridge;
import net.bridgesapi.api.player.FinancialCallback;
import net.bridgesapi.api.player.PlayerData;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.VirtualLocation;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * This file is a part of the SamaGames project
 * This code is absolutely confidential.
 * Created by zyuiop
 * (C) Copyright Elydra Network 2015
 * All rights reserved.
 */
public class City {
	private String cityName;
	private VirtualLocation spawn;
	private String fileName;

	private ArrayList<VirtualChunk> chunks = new ArrayList<>();
	private ArrayList<UUID> councils = new ArrayList<>();
	private HashMap<String, Plot> plots = new HashMap<>();
	private ArrayList<UUID> inhabitants = new ArrayList<>();
	private ArrayList<UUID> invitedUsers = new ArrayList<>();
	private HashMap<UUID, Double> taxesToPay = new HashMap<>();

	private double taxes = 0;
	private double money = 0;
	private double mayorWage = 0;
	private UUID mayor;

	private boolean requireInvite;

	public String getCityName() {
		return cityName;
	}

	public HashMap<UUID, Double> getTaxesToPay() {
		return taxesToPay;
	}

	public void setTaxesToPay(HashMap<UUID, Double> taxesToPay) {
		this.taxesToPay = taxesToPay;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public VirtualLocation getSpawn() {
		return spawn;
	}

	public void setSpawn(VirtualLocation spawn) {
		this.spawn = spawn;
	}

	public ArrayList<VirtualChunk> getChunks() {
		return chunks;
	}

	public void setChunks(ArrayList<VirtualChunk> chunks) {
		this.chunks = chunks;
	}

	public ArrayList<UUID> getCouncils() {
		return councils;
	}

	public void setCouncils(ArrayList<UUID> councils) {
		this.councils = councils;
	}

	public HashMap<String, Plot> getPlots() {
		return plots;
	}

	public void setPlots(HashMap<String, Plot> plots) {
		this.plots = plots;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public ArrayList<UUID> getInhabitants() {
		return inhabitants;
	}

	public void setInhabitants(ArrayList<UUID> inhabitants) {
		this.inhabitants = inhabitants;
	}

	public ArrayList<UUID> getInvitedUsers() {
		return invitedUsers;
	}

	public void setInvitedUsers(ArrayList<UUID> invitedUsers) {
		this.invitedUsers = invitedUsers;
	}

	public double getTaxes() {
		return taxes;
	}

	public void setTaxes(double taxes) {
		this.taxes = taxes;
	}

	public double getMayorWage() {
		return mayorWage;
	}

	public void setMayorWage(double mayorWage) {
		this.mayorWage = mayorWage;
	}

	public UUID getMayor() {
		return mayor;
	}

	public void setMayor(UUID mayor) {
		this.mayor = mayor;
	}

	public boolean isRequireInvite() {
		return requireInvite;
	}

	public void setRequireInvite(boolean requireInvite) {
		this.requireInvite = requireInvite;
	}

	public void addInhabitant(UUID inhabitant) {
		inhabitants.add(inhabitant);
	}

	public void addChunk(VirtualChunk chunk) {
		chunks.add(chunk);
	}

	public double getMoney() {
		return Math.floor(money*100) / 100;
	}

	public void setMoney(double money) {
		this.money = money;
	}

	public double countInhabitants() {
		int inhabitants = getInhabitants().size();
		HashSet<UUID> plotsInhabitants = new HashSet<>();
		for (Plot plot : plots.values()) {
			if (plot.getOwner() != null)
				plotsInhabitants.add(plot.getOwner());
		}

		int plinSize = plotsInhabitants.size();
		return Math.max(inhabitants, ((inhabitants + plinSize) / 2D));
	}

	public boolean isAdjacent(Chunk chunk) {
		VirtualChunk ch = new VirtualChunk(chunk);
		int x = ch.getX();
		int z = ch.getZ();

		return (chunks.contains(new VirtualChunk(x+1, z)) || chunks.contains(new VirtualChunk(x-1, z)) || chunks.contains(new VirtualChunk(x, z+1)) || chunks.contains(new VirtualChunk(x, z-1)));
	}

	public Plot getPlotHere(Location location) {
		for (Plot plot : plots.values()) {
			if (plot.getArea().isInside(location))
				return plot;
		}

		return null;
	}

	public boolean canBuild(Player player, Location location) {
		if (mayor.equals(player.getUniqueId()) || councils.contains(player.getUniqueId())) {
			return true;
		} else {
			Plot plot = getPlotHere(location);
			return plot != null && plot.canBuild(player, location);
		}
	}

	public void pay(UUID player, double amt) {
		Double total = taxesToPay.get(player);
		if (total != null) {
			total -= amt;
			if (total <= 0)
				taxesToPay.remove(player);
			else
				taxesToPay.put(player, total);
		}
	}

	public void payTaxes(boolean force) {
		if (this.taxes == 0)
			return;

		GregorianCalendar date = new GregorianCalendar();
		date.setTime(new Date());
		String dateString = date.get(Calendar.DAY_OF_MONTH) + "/" + date.get(Calendar.MONTH) + "/" + date.get(Calendar.YEAR);

		for (Plot plot : plots.values()) {
			if (plot.getOwner() != null) {
				UUID owner = plot.getOwner();
				PlayerData ownerData = BukkitBridge.get().getPlayerManager().getPlayerData(owner);
				if (force || !ownerData.get("lasttaxes."+getCityName(), "none").equals(dateString)) {
					double toPay = plot.getArea().getSquareArea() * taxes;
					RPMachine.getInstance().getEconomyManager().withdrawMoneyWithBalanceCheck(owner, toPay, new FinancialCallback<Double>() {
						@Override
						public void done(Double newAmount, Double difference, Throwable error) {
							if (difference != 0) {
								money += toPay;
							} else {
								ownerData.setDouble("topay."+getCityName(), ownerData.getDouble("topay."+getCityName(), 0D) + toPay);
								Double total = taxesToPay.get(owner);
								if (total == null)
									taxesToPay.put(owner, toPay);
								else
									taxesToPay.put(owner, total + toPay);
							}

							ownerData.set("lasttaxes."+getCityName(), dateString);
						}
					});
				}
			}
		}
	}

	public double simulateTaxes() {
		if (this.taxes == 0)
			return 0;

		double ret = 0;
		for (Plot plot : plots.values()) {
			ret += plot.getArea().getSquareArea() * taxes;
		}

		return ret;
	}

	public boolean canInteractWithBlock(Player player, Location location) {
		if (mayor.equals(player.getUniqueId()) || councils.contains(player.getUniqueId())) {
			return true;
		} else {
			Plot plot = getPlotHere(location);
			return (inhabitants.contains(player.getUniqueId()) && plot == null) || (plot != null && plot.canBuild(player, location));
		}
	}
}
