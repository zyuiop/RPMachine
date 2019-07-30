package net.zyuiop.rpmachine.cities.data;

import java.util.*;
import java.util.stream.Collectors;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.cities.LandOwner;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.economy.ShopOwner;
import net.zyuiop.rpmachine.economy.TaxPayer;
import net.zyuiop.rpmachine.economy.TaxPayerToken;
import net.zyuiop.rpmachine.common.Plot;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class City implements TaxPayer, LandOwner, ShopOwner {
	private String cityName;
	private VirtualLocation spawn;
	private String fileName;

	private ArrayList<VirtualChunk> chunks = new ArrayList<>();
	private ArrayList<UUID> councils = new ArrayList<>();
	private HashMap<String, Plot> plots = new HashMap<>();
	private ArrayList<UUID> inhabitants = new ArrayList<>();
	private ArrayList<UUID> invitedUsers = new ArrayList<>();
	private HashMap<TaxPayerToken, Double> taxesToPay = new HashMap<>();
	private HashMap<TaxPayerToken, Loan> loans = new HashMap<>();

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

	public HashMap<TaxPayerToken, Double> getTaxesToPay() {
		return taxesToPay;
	}

	public void setTaxesToPay(HashMap<TaxPayerToken, Double> taxesToPay) {
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
		save();
	}

	@Override
	public boolean withdrawMoney(double amount) {
		if (money < amount)
			return false;
		setMoney(money - amount);
		return true;
	}

	@Override
	public void creditMoney(double amount) {
		setMoney(money + amount);
	}

	public double countInhabitants() {
		int inhabitants = getInhabitants().size();
		HashSet<TaxPayerToken> plotsInhabitants = plots.values().stream().filter(plot -> plot.getOwner() != null).map(Plot::getOwner).collect(Collectors.toCollection(HashSet::new));

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

	public void pay(TaxPayer player, double amt) {
		TaxPayerToken token = TaxPayerToken.fromPayer(player);
		Double total = taxesToPay.get(token);
		if (total != null) {
			total -= amt;
			if (total <= 0)
				taxesToPay.remove(token);
			else
				taxesToPay.put(token, total);
		}
	}

	public void payTaxes(boolean force) {
		if (this.taxes == 0)
			return;

		for (Plot plot : plots.values()) {
			if (plot.getOwner() != null) {
				TaxPayerToken owner = plot.getOwner();
				TaxPayer ownerData = owner.getTaxPayer();
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

	@Override
	public boolean canManagePlot(Player player) {
		return mayor.equals(player.getUniqueId());
	}

	@Override
	public boolean canManageShop(Player player) {
		return mayor.equals(player.getUniqueId());
	}

	public Loan getLoan(TaxPayerToken token) {
		return loans.get(token);
	}

	public double payLoan(TaxPayerToken token, double amount) {
		Loan loan = getLoan(token);
		if (loan == null) {
			return -1;
		}

		if (loan.getAmountToPay() < amount) {
			return -2;
		}

		double remaining = loan.pay(amount);
		if (remaining < 0.01) {
			loans.remove(token);
			remaining = 0;
		}

		save();
		return remaining;
	}

	public HashMap<TaxPayerToken, Loan> getLoans() {
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

	public static class Loan {
		private TaxPayerToken borrower;
		private double amountBorrowed;
		private double interestRate;
		private double amountPaid = 0;
		private Date maximumDate;

		public Loan(TaxPayerToken borrower, double amountBorrowed, double interestRate, Date maximumDate) {
			this.borrower = borrower;
			this.amountBorrowed = amountBorrowed;
			this.interestRate = interestRate;
			this.maximumDate = maximumDate;
		}

		public Loan() {
		}

		public TaxPayerToken getBorrower() {
			return borrower;
		}

		public void setBorrower(TaxPayerToken borrower) {
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
	}
}
