package net.zyuiop.rpmachine.auctions;

import com.google.gson.reflect.TypeToken;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.json.Json;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Louis Vialar
 */
public class AuctionManager {
    public static final AuctionManager INSTANCE = new AuctionManager();

    private AuctionManager() {
    }

    private final File folder = new File(RPMachine.getInstance().getDataFolder().getPath());

    private final File auctionsFile = new File(folder, "auctions.json");
    private final File buysFile = new File(folder, "buy_orders.json");
    private final File typesFile = new File(folder, "auction_types.json");
    private final File transactionsFile = new File(folder, "transactions.json");

    private final Map<Material, TreeSet<SellOrder>> sellOffers = new HashMap<>();
    private final Map<Material, TreeSet<BuyOrder>> buyOffers = new HashMap<>();
    private final Map<Integer, AbstractTransaction> transactions = new HashMap<>();
    private final Map<String, AuctionType> types = new HashMap<>();

    public void load() {
        try {
            if (auctionsFile.exists()) {
                TypeToken<Map<Material, TreeSet<SellOrder>>> tt = new TypeToken<Map<Material, TreeSet<SellOrder>>>() {
                };
                this.sellOffers.putAll(Json.GSON.fromJson(new FileReader(auctionsFile), tt.getType()));
            }

            if (buysFile.exists()) {
                TypeToken<Map<Material, TreeSet<BuyOrder>>> tt = new TypeToken<Map<Material, TreeSet<BuyOrder>>>() {
                };
                this.buyOffers.putAll(Json.GSON.fromJson(new FileReader(buysFile), tt.getType()));
            }

            if (typesFile.exists()) {
                TypeToken<Map<String, AuctionType>> tt = new TypeToken<Map<String, AuctionType>>() {
                };
                this.types.putAll(Json.GSON.fromJson(new FileReader(typesFile), tt.getType()));
            }

            if (transactionsFile.exists()) {
                TypeToken<Map<Integer, BuyTransaction>> tt = new TypeToken<Map<Integer, BuyTransaction>>() {
                };
                this.transactions.putAll(Json.GSON.fromJson(new FileReader(transactionsFile), tt.getType()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Cancel all existing transactions (crash backup!)
        new ArrayList<>(transactions.values()).forEach(AbstractTransaction::cancel);
        save();

        // Schedule auto purge
        Bukkit.getScheduler().runTaskTimer(RPMachine.getInstance(), this::purge, 15 * 20L, 15 * 20L);

        Bukkit.getPluginManager().registerEvents(AuctionInventoryListener.INSTANCE, RPMachine.getInstance());
    }

    public void stop() {
        // Cancel all existing transactions
        new ArrayList<>(transactions.values()).forEach(AbstractTransaction::cancel);

        // Re-save
        doSave();
    }

    public void purge() {
        // We clone the list to avoid ConcurrentModificationExceptions
        new ArrayList<>(transactions.values()).forEach(AbstractTransaction::checkAutoCancel);

        save();
    }

    private void doSave() {
        try {
            if (!auctionsFile.exists()) auctionsFile.createNewFile();
            if (!buysFile.exists()) buysFile.createNewFile();
            if (!transactionsFile.exists()) transactionsFile.createNewFile();
            if (!typesFile.exists()) typesFile.createNewFile();

            Json.GSON.toJson(this.sellOffers, new PrintStream(auctionsFile));
            Json.GSON.toJson(this.buyOffers, new PrintStream(buysFile));
            Json.GSON.toJson(this.types, new PrintStream(typesFile));
            Json.GSON.toJson(this.transactions, new PrintStream(transactionsFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, AuctionType> getTypes() {
        return Collections.unmodifiableMap(types);
    }

    public AuctionType getType(String id) {
        return types.get(id.toLowerCase());
    }

    public void createType(AuctionType type) {
        types.put(type.getId(), type);
        save();
    }

    public void save() {
        save(false);
    }

    public void save(boolean now) {
        if (now) doSave();
        else
            Bukkit.getScheduler().runTaskAsynchronously(RPMachine.getInstance(), this::doSave);
    }

    public double averagePrice(Material material) {
        if (!sellOffers.containsKey(material) && !buyOffers.containsKey(material)) return Double.NaN;

        if (sellOffers.get(material).isEmpty() && buyOffers.get(material).isEmpty()) return Double.NaN;

        double total = 0D;
        int items = 0;

        if (sellOffers.containsKey(material)) for (SellOrder a : sellOffers.get(material)) {
            total += a.getItemPrice() * a.getAvailable();
            items += a.getAvailable();
        }

        if (buyOffers.containsKey(material)) for (BuyOrder a : buyOffers.get(material)) {
            total += a.getItemPrice() * a.getAvailable();
            items += a.getAvailable();
        }

        return total / items;
    }

    public int countAvailable(Material material) {
        if (!sellOffers.containsKey(material)) return 0;

        if (sellOffers.get(material).isEmpty()) return 0;

        return sellOffers.get(material).stream().mapToInt(SellOrder::getAvailable).sum();
    }

    public double minPrice(Material material) {
        if (!sellOffers.containsKey(material) || sellOffers.get(material).isEmpty()) return Double.NaN;

        return sellOffers.get(material).first().getItemPrice();
    }

    public List<SellOrder> getMyAuctions(Material material, LegalEntity entity) {
        if (sellOffers.containsKey(material)) {
            return sellOffers.get(material).stream().filter(a -> a.ownerTag().equals(entity.tag())).collect(Collectors.toList());
        } else return Collections.emptyList();
    }

    public boolean removeAuction(SellOrder auction) {
        if (sellOffers.containsKey(auction.getMaterial()))
            return sellOffers.get(auction.getMaterial()).remove(auction);
        return false;
    }

    public boolean removeAuction(BuyOrder auction) {
        if (buyOffers.containsKey(auction.getMaterial()))
            return buyOffers.get(auction.getMaterial()).remove(auction);
        return false;
    }

    public boolean hasAuction(SellOrder auction) {
        if (sellOffers.containsKey(auction.getMaterial()))
            return sellOffers.get(auction.getMaterial()).contains(auction);
        return false;
    }

    public void addAuction(SellOrder auction) {
        if (!sellOffers.containsKey(auction.getMaterial()) || sellOffers.get(auction.getMaterial()) == null)
            sellOffers.put(auction.getMaterial(), new TreeSet<>());

        sellOffers.get(auction.getMaterial()).add(auction);
    }

    public void addAuction(BuyOrder auction) {
        if (!buyOffers.containsKey(auction.getMaterial()) || buyOffers.get(auction.getMaterial()) == null)
            buyOffers.put(auction.getMaterial(), new TreeSet<>());

        buyOffers.get(auction.getMaterial()).add(auction);
    }

    public TreeSet<SellOrder> getAuctions(Material itemType) {
        return sellOffers.containsKey(itemType) ? sellOffers.get(itemType) : new TreeSet<>();
    }


    public BuyTransaction startBuy(LegalEntity buyer, Material material, int amount) {
        if (!sellOffers.containsKey(material) || sellOffers.get(material).isEmpty())
            return null;

        TreeSet<SellOrder> target = new TreeSet<>();
        Iterator<SellOrder> auctionIterator = sellOffers.get(material).iterator();
        int total = 0;
        while (total < amount && auctionIterator.hasNext()) {
            SellOrder a = auctionIterator.next();
            total += a.getAvailable();
            target.add(a);
            auctionIterator.remove();
        }

        BuyTransaction tx = new BuyTransaction(buyer, Math.min(amount, total));
        tx.getContent().addAll(target);

        transactions.put(tx.getId(), tx);
        save();

        return tx;
    }

    public BuyTransaction startBuyRemovedOrder(LegalEntity buyer, SellOrder auction) {
        BuyTransaction tx = new BuyTransaction(buyer, auction.getAvailable());
        tx.getContent().add(auction);

        transactions.put(tx.getId(), tx);
        save();

        return tx;
    }

    public SellTransaction startSell(LegalEntity seller, Material material, int amount) {
        if (!buyOffers.containsKey(material) || buyOffers.get(material).isEmpty())
            return null;

        TreeSet<BuyOrder> target = new TreeSet<>();
        Iterator<BuyOrder> auctionIterator = buyOffers.get(material).iterator();
        int total = 0;
        while (total < amount && auctionIterator.hasNext()) {
            BuyOrder a = auctionIterator.next();
            if (a.getRemainingItems() <= 0)
                continue;

            total += a.getRemainingItems();
            target.add(a);
            auctionIterator.remove();
        }

        if (total == 0)
            return null;

        SellTransaction tx = new SellTransaction(seller, Math.min(amount, total));
        tx.getContent().addAll(target);

        transactions.put(tx.getId(), tx);
        save();

        return tx;
    }

    public SellTransaction startBuyRemovedOrder(LegalEntity seller, BuyOrder auction) {
        SellTransaction tx = new SellTransaction(seller, auction.getRemainingItems());
        tx.getContent().add(auction);

        transactions.put(tx.getId(), tx);
        save();

        return tx;
    }


    public void removeTransaction(AbstractTransaction tx) {
        removeTransaction(tx, true);
    }

    public void removeTransaction(AbstractTransaction tx, boolean save) {
        transactions.remove(tx.getId());
        if (save) save();
    }

    public List<BuyOrder> getMyBuys(Material material, LegalEntity entity) {
        if (buyOffers.containsKey(material)) {
            return buyOffers.get(material).stream().filter(a -> a.ownerTag().equals(entity.tag())).collect(Collectors.toList());
        } else return Collections.emptyList();
    }
}
