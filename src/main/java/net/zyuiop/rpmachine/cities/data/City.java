package net.zyuiop.rpmachine.cities.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.database.PlayerData;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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

	private static boolean sameDay(Date target) {
		GregorianCalendar date = new GregorianCalendar();
		date.setTime(new Date());

		GregorianCalendar compare = new GregorianCalendar();
		compare.setTime(target);

		return compare.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH) && date.get(Calendar.MONTH) == compare.get(Calendar.MONTH) && date.get(Calendar.YEAR) == compare.get(Calendar.YEAR);
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public HashMap<UUID, Double> getTaxesToPay() {
		return taxesToPay;
	}

	public void setTaxesToPay(HashMap<UUID, Double> taxesToPay) {
		this.taxesToPay = taxesToPay;
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
		return Math.floor(money * 100) / 100;
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

		return (chunks.contains(new VirtualChunk(x + 1, z)) || chunks.contains(new VirtualChunk(x - 1, z)) || chunks.contains(new VirtualChunk(x, z + 1)) || chunks.contains(new VirtualChunk(x, z - 1)));
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

		for (Plot plot : plots.values()) {
			if (plot.getOwner() != null) {
				UUID owner = plot.getOwner();
				PlayerData ownerData = RPMachine.database().getPlayerData(owner);
				Date lastPaid = ownerData.getLastTaxes(getCityName());

				if (force || lastPaid == null || !sameDay(lastPaid)) {
					double toPay = plot.getArea().getSquareArea() * taxes;
					RPMachine.getInstance().getEconomyManager().withdrawMoneyWithBalanceCheck(owner, toPay, (newAmount, difference) -> {
						if (difference != 0) {
							money += toPay;
						} else {
							double lateTaxes = ownerData.getUnpaidTaxes(getCityName());
							lateTaxes += toPay;
							ownerData.setUnpaidTaxes(getCityName(), lateTaxes);

							Double total = taxesToPay.get(owner);
							if (total == null)
								taxesToPay.put(owner, toPay);
							else
								taxesToPay.put(owner, total + toPay);
						}

						ownerData.setLastTaxes(getCityName(), new Date());
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
