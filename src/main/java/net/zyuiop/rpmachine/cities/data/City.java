package net.zyuiop.rpmachine.cities.data;

import java.util.*;
import java.util.stream.Collectors;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.economy.RoleToken;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.entities.Ownable;
import net.zyuiop.rpmachine.permissions.DelegatedPermission;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

// TODO: finish implementing loans
public class City implements LegalEntity {
	private String cityName;
	private VirtualLocation spawn;
	private String fileName;

	private final Set<VirtualChunk> chunks = new HashSet<>();
	private final Map<UUID, Set<Permission>> councils = new HashMap<>();
	private final Map<String, Plot> plots = new HashMap<>();
	private final Set<UUID> inhabitants = new HashSet<>();
	private final Set<UUID> invitedUsers = new HashSet<>();
	private final Map<String, Double> taxesToPay = new HashMap<>();
	private final Map<String, Loan> loans = new HashMap<>();

	private CityTaxPayer taxPayer = new CityTaxPayer(); // loaded by Gson

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

	public Map<String, Double> getTaxesToPay() {
		return taxesToPay;
	}

	public VirtualLocation getSpawn() {
		return spawn;
	}

	public void setSpawn(VirtualLocation spawn) {
		this.spawn = spawn;
	}

	public Set<VirtualChunk> getChunks() {
		return chunks;
	}

	public Set<UUID> getCouncils() {
		return councils.keySet();
	}

	public Map<String, Plot> getPlots() {
		return plots;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Set<UUID> getInhabitants() {
		return inhabitants;
	}

	public Set<UUID> getInvitedUsers() {
		return invitedUsers;
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

	public double getBalance() {
		return Math.floor(money * 100) / 100;
	}

	public void setBalance(double money) {
		this.money = money;
		save();
	}

	@Override
	public boolean withdrawMoney(double amount) {
		if (money < amount)
			return false;
		setBalance(money - amount);
		return true;
	}

	@Override
	public void creditMoney(double amount) {
		setBalance(money + amount);
	}

	@Override
	public String displayable() {
		return ChatColor.DARK_AQUA + getCityName();
	}

	@Override
	public String shortDisplayable() {
		return ChatColor.AQUA + "(Ville) " + ChatColor.DARK_AQUA + getCityName();
	}

	public double countInhabitants() {
		int inhabitants = getInhabitants().size();
		HashSet<String> plotsInhabitants = plots.values().stream().filter(plot -> plot.getOwner() != null && plot.owner() != this).map(Plot::getOwner).collect(Collectors.toCollection(HashSet::new));

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
		// TODO: update to permissions
		if (mayor.equals(player.getUniqueId()) || councils.containsKey(player.getUniqueId())) {
			return true;
		} else {
			Plot plot = getPlotHere(location);
			return plot != null && plot.canBuild(player, location);
		}
	}

	public void pay(LegalEntity payer, double amt) {
		Double total = taxesToPay.get(payer.tag());
		if (total != null) {
			total -= amt;
			if (total <= 0)
				taxesToPay.remove(payer.tag());
			else
				taxesToPay.put(payer.tag(), total);
		}
	}

	public void payTaxes(boolean force) {
		if (this.taxes == 0)
			return;

		for (Plot plot : plots.values()) {
			if (plot.getOwner() != null) {
				LegalEntity ownerData = plot.owner();
				if (ownerData == this)
					continue; // City doesn't pay taxes to itself

				String owner = plot.ownerTag();
				Date lastPaid = ownerData.getLastTaxes(getCityName());

				if (force || lastPaid == null || !sameDay(lastPaid)) {
					double toPay = plot.getArea().getSquareArea() * taxes;
					RPMachine.getInstance().getEconomyManager().withdrawMoneyWithBalanceCheck(ownerData, toPay, (newAmount, difference) -> {
						if (!difference) {
							money += toPay;
						} else {
							double lateTaxes = ownerData.getUnpaidTaxes(getCityName());
							lateTaxes += toPay;
							ownerData.setUnpaidTaxes(getCityName(), lateTaxes);

							taxesToPay.merge(owner, toPay, Double::sum);
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
		// TODO: update to permissions
		if (mayor.equals(player.getUniqueId()) || councils.containsKey(player.getUniqueId())) {
			return true;
		} else {
			Plot plot = getPlotHere(location);
			return (inhabitants.contains(player.getUniqueId()) && plot == null) || (plot != null && plot.canBuild(player, location));
		}
	}

	@Override
	public void setUnpaidTaxes(String city, double amount) {
		taxPayer.getUnpaidTaxes().put(city, amount);
		save();
	}

	public void save() {
		RPMachine.getInstance().getCitiesManager().saveCity(this);
	}

	@Override
	public double getUnpaidTaxes(String city) {
		return taxPayer.getUnpaidTaxes().get(city);
	}

	@Override
	public void setLastTaxes(String city, Date date) {
		taxPayer.getLastPaidTaxes().put(city, date);
		save();
	}

	@Override
	public Date getLastTaxes(String city) {
		return taxPayer.getLastPaidTaxes().get(city);
	}

	@Override
	public Map<String, Double> getUnpaidTaxes() {
		return taxPayer.getUnpaidTaxes();
	}

	public boolean hasPermission(Player player, @Nonnull Permission permission) {
		if (mayor.equals(player.getUniqueId())) {
			return true; // Mayor has all permissions on all city properties
		} else {
			if (councils.containsKey(player.getUniqueId())) {
				return councils.get(player.getUniqueId()).contains(permission);
			}
			return false;
		}
	}

	@Override
	public boolean hasDelegatedPermission(Player player, @Nonnull DelegatedPermission permission) {
		return hasPermission(player, permission);
	}

	public Loan getLoan(LegalEntity token) {
		return loans.get(token.tag());
	}

	public double payLoan(LegalEntity token, double amount) {
		Loan loan = getLoan(token);
		if (loan == null) {
			return -1;
		}

		if (loan.getAmountToPay() < amount) {
			return -2;
		}

		double remaining = loan.pay(amount);
		if (remaining < 0.01) {
			loans.remove(token.tag());
			remaining = 0;
		}

		save();
		return remaining;
	}

	public Map<String, Loan> getLoans() {
		return loans;
	}

	public Collection<Loan> getExpiredLoans() {
		return getLoans().values().stream().filter(l -> l.getMaximumDate().before(new Date())).collect(Collectors.toList());
	}

	public void updateLoan(Loan loan) {
		loans.put(loan.getBorrower(), loan);
		save();
	}

	public static class CityTaxPayer {
		private Map<String, Double> unpaidTaxes = new HashMap<>();
		private Map<String, Date> lastPaidTaxes = new HashMap<>();

		Map<String, Double> getUnpaidTaxes() {
			return unpaidTaxes;
		}

		public void setUnpaidTaxes(Map<String, Double> unpaidTaxes) {
			this.unpaidTaxes = unpaidTaxes;
		}

		Map<String, Date> getLastPaidTaxes() {
			return lastPaidTaxes;
		}

		public void setLastPaidTaxes(Map<String, Date> lastPaidTaxes) {
			this.lastPaidTaxes = lastPaidTaxes;
		}
	}

	public static class Loan implements Ownable {
		private String borrower;
		private double amountBorrowed;
		private double interestRate;
		private double amountPaid = 0;
		private Date maximumDate;

		public Loan(String borrower, double amountBorrowed, double interestRate, Date maximumDate) {
			this.borrower = borrower;
			this.amountBorrowed = amountBorrowed;
			this.interestRate = interestRate;
			this.maximumDate = maximumDate;
		}

		public Loan() {
		}

		public String getBorrower() {
			return borrower;
		}

		public void setBorrower(String borrower) {
			this.borrower = borrower;
		}

		public double getAmountBorrowed() {
			return amountBorrowed;
		}

		public void setAmountBorrowed(double amountBorrowed) {
			this.amountBorrowed = amountBorrowed;
		}

		public double getInterestRate() {
			return interestRate;
		}

		public void setInterestRate(double interestRate) {
			this.interestRate = interestRate;
		}

		public double getAmountPaid() {
			return amountPaid;
		}

		public void setAmountPaid(double amountPaid) {
			this.amountPaid = amountPaid;
		}

		public double getAmountToPay() {
			return (amountBorrowed * (1 + interestRate)) - getAmountPaid();
		}

		public double pay(double amount) {
			amountPaid += amount;
			return getAmountToPay();
		}

		public Date getMaximumDate() {
			return maximumDate;
		}

		public void setMaximumDate(Date maximumDate) {
			this.maximumDate = maximumDate;
		}

		@Nullable
		@Override
		public String ownerTag() {
			return borrower;
		}
	}
}
