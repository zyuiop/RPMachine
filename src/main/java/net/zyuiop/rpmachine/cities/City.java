package net.zyuiop.rpmachine.cities;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.common.VirtualLocation;
import net.zyuiop.rpmachine.cities.politics.PoliticalSystem;
import net.zyuiop.rpmachine.cities.politics.StateOfRights;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.common.VirtualChunk;
import net.zyuiop.rpmachine.database.StoredEntity;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.json.JsonExclude;
import net.zyuiop.rpmachine.permissions.CityPermissions;
import net.zyuiop.rpmachine.permissions.DelegatedPermission;
import net.zyuiop.rpmachine.permissions.Permission;
import org.bukkit.*;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class City implements LegalEntity, StoredEntity {
    private final Set<VirtualChunk> chunks = new HashSet<>();
    private final Map<UUID, Set<Permission>> councils = new HashMap<>();
    private final Map<String, Plot> plots = new HashMap<>();
    private final Set<UUID> inhabitants = new HashSet<>();
    private final Set<UUID> invitedUsers = new HashSet<>();
    private final Map<String, Double> taxesToPay = new HashMap<>();

    private String cityName;
    private VirtualLocation spawn;
    private String fileName;
    private CityTaxPayer taxPayer = new CityTaxPayer(); // loaded by Gson

    private long privateChannelId = 0;
    private long publicChannelId = 0;

    @JsonExclude
    private PoliticalSystem politicalSystem = StateOfRights.INSTANCE; // todo: make possible to change

    private double taxes = 0;
    private double money = 0;
    private double mayorWage = 0;
    private int joinTax = 0;
    private int tpTax = 0;
    private double plotSellTaxRate = 0;
    private double vat = 0;
    private UUID mayor;
    private ChatColor chatColor;
    private boolean allowSpawn;

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

    public List<Line> getBorders() {
        List<Line> line = new ArrayList<>();
        for (VirtualChunk vc : chunks) {
            if (!chunks.contains(vc.add(-1, 0))) line.add(vc.line(0, 0, 0, 15));
            if (!chunks.contains(vc.add(0, -1))) line.add(vc.line(0, 0, 15, 0));
            if (!chunks.contains(vc.add(1, 0))) line.add(vc.line(15, 0, 15, 15));
            if (!chunks.contains(vc.add(0, 1))) line.add(vc.line(0, 15, 15, 15));
        }
        Bukkit.getLogger().info(line.toString());
        return line;
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

    public Set<UUID> getAdministrators() {
        Set<UUID> set = new HashSet<>(councils.keySet());
        set.add(mayor);
        return set;
    }

    public Map<String, Plot> getPlots() {
        return plots;
    }

    public void addPlot(String name, Plot plot) {
        plots.put(name.toLowerCase(), plot);
        save();
    }

    public void removePlot(String name) {
        plots.remove(name.toLowerCase());
        save();
    }

    public Plot getPlot(String name) {
        return plots.get(name.toLowerCase());
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


    public Set<Player> getOnlineInhabitants() {
        return getInhabitants().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(OfflinePlayer::isOnline)
                .collect(Collectors.toSet());
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

	public int getJoinTax() {
		return joinTax;
	}

	public void setJoinTax(int joinTax) {
		this.joinTax = joinTax;
	}

	public int getTpTax() {
		return tpTax;
	}

	public void setTpTax(int tpTax) {
		this.tpTax = tpTax;
	}

    public double getVat() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat = vat;
    }

    public double getPlotSellTaxRate() {
		return plotSellTaxRate;
	}

	public void setPlotSellTaxRate(double plotSellTaxRate) {
		this.plotSellTaxRate = Math.min(plotSellTaxRate, 1.0);
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
    public String shortDisplayable() {
        return ChatColor.DARK_AQUA + getCityName();
    }

    @Override
    public String displayable() {
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

    public void payTaxes(LegalEntity payer, double amt) {
        Double total = taxesToPay.get(payer.tag());
        if (total != null) {
            total -= amt;
            if (total <= 0)
                taxesToPay.remove(payer.tag());
            else
                taxesToPay.put(payer.tag(), total);
        }
    }

    public void requestTaxes(boolean force) {
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
                    double toPay = plot.getArea().computeArea() * taxes;

                    if (!ownerData.transfer(toPay, this)) {
                        double lateTaxes = ownerData.getUnpaidTaxes(getCityName());
                        lateTaxes += toPay;
                        ownerData.setUnpaidTaxes(getCityName(), lateTaxes);
                        taxesToPay.merge(owner, toPay, Double::sum);
                    } else {
                        money += toPay;
                    }

                    ownerData.setLastTaxes(getCityName(), new Date());
                }
            }
        }
    }

    public void cleanPlots() {
        Date now = new Date();
        Set<String> toDelete = plots.entrySet().stream().filter(p -> p.getValue().isDueForDeletion() && p.getValue().getDeletionDate().before(now)).map(Map.Entry::getKey).collect(Collectors.toSet());
        if (!toDelete.isEmpty()) {
            RPMachine.getInstance().getLogger().info("City " + cityName + " : cleaning empty plots " + toDelete.toString());
            toDelete.forEach(plots::remove);
            save();
        }
    }

    public void sendWarnings() {
        Set<Plot> toDelete = plots.values().stream().filter(Plot::isDueForDeletion).collect(Collectors.toSet());
        if (!toDelete.isEmpty()) {
            toDelete.forEach(p -> p.sendDeletionWarning(cityName));
        }
    }

    public double simulateTaxes() {
        if (this.taxes == 0)
            return 0;

        double ret = 0;
        for (Plot plot : plots.values()) {
            ret += plot.getArea().computeArea() * taxes;
        }

        return ret;
    }

    public boolean canInteractWithBlock(Player player, Location location) {
        Plot plot = getPlotHere(location);

        if (plot == null) {
            // A voir, tous les habitants de la ville peuvent-t-ils vraiment intéragir dans toutes les parcelles ?
            return inhabitants.contains(player.getUniqueId());
        } else {
            if (plot.getOwner() == null || plot.getOwner().equalsIgnoreCase(tag()))
                return hasPermission(player, CityPermissions.INTERACT_IN_EMPTY_PLOTS);
            return hasPermission(player, CityPermissions.INTERACT_IN_PLOTS) || plot.canBuild(player, location);
        }
    }

    public boolean canBuild(Player player, Location location) {
        Plot plot = getPlotHere(location);

        if (plot == null) {
            return hasPermission(player, CityPermissions.BUILD_IN_CITY);
        } else {
            if (plot.getOwner() == null || plot.getOwner().equalsIgnoreCase(tag()))
                return hasPermission(player, CityPermissions.BUILD_IN_EMPTY_PLOTS);
            return hasPermission(player, CityPermissions.BUILD_IN_PLOTS) || plot.canBuild(player, location);
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
        return hasPermission(player.getUniqueId(), permission);
    }

    public boolean hasPermission(UUID player, @Nonnull Permission permission) {
        if (politicalSystem.isRestricted(permission))
            return false;

        if (mayor.equals(player)) {
            return true; // Mayor has all permissions on all city properties
        } else {
            if (councils.containsKey(player)) {
                return councils.get(player).contains(permission);
            }
            return false;
        }
    }

    public PoliticalSystem getPoliticalSystem() {
        return politicalSystem;
    }

    @Override
    public boolean hasDelegatedPermission(Player player, @Nonnull DelegatedPermission permission) {
        return hasPermission(player, permission);
    }

    public boolean canActAs(Player p) {
        if (mayor.equals(p.getUniqueId()))
            return true;

        if (councils.containsKey(p.getUniqueId()))
            return true;

        return false;
    }

    public void addCouncil(UUID id) {
        if (!councils.containsKey(id))
            councils.put(id, new HashSet<>());
    }

    public void removeCouncil(UUID id) {
        councils.remove(id);
    }

    public void addPermission(UUID target, Permission permission) {
        if (councils.containsKey(target)) {
            councils.get(target).add(permission);
            save();
        }
    }

    public void removePermission(UUID target, Permission permission) {
        if (councils.containsKey(target)) {
            councils.get(target).remove(permission);
            save();
        }
    }

    public long getPrivateChannelId() {
        return privateChannelId;
    }

    public void setPrivateChannelId(long privateChannelId) {
        this.privateChannelId = privateChannelId;
    }

    public long getPublicChannelId() {
        return publicChannelId;
    }

    public void setPublicChannelId(long publicChannelId) {
        this.publicChannelId = publicChannelId;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public void setChatColor(ChatColor chatColor) {
        this.chatColor = chatColor;
    }

    public boolean isAllowSpawn() {
        return allowSpawn;
    }

    public void setAllowSpawn(boolean allowSpawn) {
        this.allowSpawn = allowSpawn;
    }

    public static class CityTaxPayer {
        private Map<String, Double> unpaidTaxes = new HashMap<>();
        private Map<String, Date> lastPaidTaxes = new HashMap<>();

        Map<String, Double> getUnpaidTaxes() {
            return unpaidTaxes;
        }

        Map<String, Date> getLastPaidTaxes() {
            return lastPaidTaxes;
        }
    }
}
