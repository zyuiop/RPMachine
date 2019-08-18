package net.zyuiop.rpmachine.auctions;

import com.google.gson.reflect.TypeToken;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.entities.Ownable;
import net.zyuiop.rpmachine.json.Json;
import net.zyuiop.rpmachine.json.JsonExclude;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
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

    private static int TX_ID = 0;
    private static long TX_EXPIRATION_TIME = 20 * 1000L;

    private final File folder = new File(RPMachine.getInstance().getDataFolder().getPath());
    private final Map<Material, TreeSet<Auction>> auctions = new HashMap<>();
    private final Map<Integer, Transaction> transactions = new HashMap<>();

    public void load() {
        try {
            File auctions = new File(folder, "auctions.json");
            File transactions = new File(folder, "transactions.json");

            if (auctions.exists()) {
                TypeToken<Map<Material, TreeSet<Auction>>> tt = new TypeToken<Map<Material, TreeSet<Auction>>>() {
                };
                this.auctions.putAll(Json.GSON.fromJson(new FileReader(auctions), tt.getType()));
            }

            if (transactions.exists()) {
                TypeToken<Map<Integer, Transaction>> tt = new TypeToken<Map<Integer, Transaction>>() {
                };
                this.transactions.putAll(Json.GSON.fromJson(new FileReader(transactions), tt.getType()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Cancel all existing transactions (crash backup!)
        new ArrayList<>(transactions.values()).forEach(Transaction::cancel);
        save();

        // Schedule auto purge
        Bukkit.getScheduler().runTaskTimer(RPMachine.getInstance(), this::purge, 15 * 20L, 15 * 20L);

        Bukkit.getPluginManager().registerEvents(AuctionInventoryListener.INSTANCE, RPMachine.getInstance());
    }

    public void stop() {
        // Cancel all existing transactions
        new ArrayList<>(transactions.values()).forEach(Transaction::cancel);

        // Re-save
        doSave();
    }

    public void purge() {
        // We clone the list to avoid ConcurrentModificationExceptions
        new ArrayList<>(transactions.values()).forEach(Transaction::checkAutoCancel);

        save();
    }

    private void doSave() {
        File auctions = new File(folder, "auctions.json");
        File transactions = new File(folder, "transactions.json");

        try {
            if (!auctions.exists()) auctions.createNewFile();
            if (!transactions.exists()) transactions.createNewFile();

            Json.GSON.toJson(this.auctions, new PrintStream(auctions));
            Json.GSON.toJson(this.transactions, new PrintStream(transactions));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if (!auctions.containsKey(material)) return Double.NaN;

        if (auctions.get(material).isEmpty()) return Double.NaN;

        double total = 0D;
        int items = 0;

        for (Auction a : auctions.get(material)) {
            total += a.getItemPrice() * a.getAvailable();
            items += a.getAvailable();
        }

        return total / items;
    }

    public int countAvailable(Material material) {
        if (!auctions.containsKey(material)) return 0;

        if (auctions.get(material).isEmpty()) return 0;

        return auctions.get(material).stream().mapToInt(Auction::getAvailable).sum();
    }

    public double minPrice(Material material) {
        if (!auctions.containsKey(material) || auctions.get(material).isEmpty()) return Double.NaN;

        return auctions.get(material).first().getItemPrice();
    }

    public List<Auction> getMyAuctions(Material material, LegalEntity entity) {
        if (auctions.containsKey(material)) {
            return auctions.get(material).stream().filter(a -> a.ownerTag().equals(entity.tag())).collect(Collectors.toList());
        } else return Collections.emptyList();
    }

    public boolean removeAuction(Auction auction) {
        if (auctions.containsKey(auction.getMaterial()))
            return auctions.get(auction.getMaterial()).remove(auction);
        return false;
    }

    public boolean hasAuction(Auction auction) {
        if (auctions.containsKey(auction.getMaterial()))
            return auctions.get(auction.getMaterial()).contains(auction);
        return false;
    }

    public void addAuction(Auction auction) {
        if (!auctions.containsKey(auction.getMaterial()) || auctions.get(auction.getMaterial()) == null)
            auctions.put(auction.getMaterial(), new TreeSet<>());

        auctions.get(auction.getMaterial()).add(auction);
    }

    public TreeSet<Auction> getAuctions(Material itemType) {
        return auctions.containsKey(itemType) ? auctions.get(itemType) : new TreeSet<>();
    }

    public static class Transaction implements Ownable {
        private final int id;
        private final String buyer;
        private final long startTime;
        private final TreeSet<Auction> content;
        private final int requestedItems;

        @JsonExclude
        private boolean canceled = false;

        public Transaction(LegalEntity buyer, int requestedItems) {
            this.id = TX_ID++;
            this.startTime = System.currentTimeMillis();
            this.content = new TreeSet<>();
            this.buyer = buyer.tag();
            this.requestedItems = requestedItems;
        }

        public int getRequestedItems() {
            return requestedItems;
        }

        public int remainingSeconds() {
            long end = startTime + TX_EXPIRATION_TIME;
            long remain = end - System.currentTimeMillis();

            return (remain < 0 ? 0 : (int) (remain / 1000));
        }

        @Nullable
        @Override
        public String ownerTag() {
            return buyer;
        }

        public void cancel() {
            if (canceled)
                return;
            canceled = true;

            Iterator<Auction> iter = content.iterator();

            while (iter.hasNext()) {
                Auction next = iter.next();
                if (next != null)
                    INSTANCE.addAuction(next);
                iter.remove();
            }
            INSTANCE.transactions.remove(id);
        }

        boolean checkAutoCancel() {
            if (System.currentTimeMillis() > startTime + TX_EXPIRATION_TIME) {
                cancel();
            }

            return canceled;
        }

        public boolean complete(Player player) {
            checkAutoCancel();

            if (canceled) {
                player.sendMessage(ChatColor.RED + "Transaction déjà acceptée ou invalide.");
                INSTANCE.transactions.remove(id);
                return false;
            }


            double price = getPrice();
            if (owner().getBalance() < price) {
                Messages.notEnoughMoneyEntity(player, owner(), price);
                cancel();
                return true;
            }

            canceled = true;

            Iterator<Auction> auctionIterator = content.iterator();
            int remaining = requestedItems;
            while (remaining > 0) {
                Auction a = auctionIterator.next();

                if (remaining < a.getAvailable()) {
                    Auction toAdd = a.buy(remaining, player, owner());

                    Bukkit.getLogger().info("Adding remainings of auction " + a.getItemPrice() + " " + a.getMaterial() + " " + a.ownerTag());

                    INSTANCE.addAuction(toAdd);
                    INSTANCE.transactions.remove(id);
                    INSTANCE.save();
                    return true;
                } else {
                    Auction ret = a.buy(a.getAvailable(), player, owner());

                    if (ret == a)
                        return false; // Not enough money

                    remaining -= a.getAvailable();
                }

                auctionIterator.remove();
            }

            INSTANCE.transactions.remove(id);
            INSTANCE.save();
            return true;
        }

        public double getPrice() {
            Iterator<Auction> auctionIterator = content.iterator();
            int remaining = requestedItems;
            double price = 0;
            while (remaining > 0) {
                Auction a = auctionIterator.next();

                if (remaining < a.getAvailable()) {
                    price += remaining * a.getItemPrice();
                } else {
                    price += a.getItemPrice() * a.getAvailable();
                }

                remaining -= a.getAvailable();
            }

            return price;
        }

    }


    public Transaction startTransaction(LegalEntity buyer, Material material, int amount) {
        if (!auctions.containsKey(material) || auctions.get(material).isEmpty())
            return null;

        TreeSet<Auction> target = new TreeSet<>();
        Iterator<Auction> auctionIterator = auctions.get(material).iterator();
        int total = 0;
        while (total < amount && auctionIterator.hasNext()) {
            Auction a = auctionIterator.next();
            total += a.getAvailable();
            target.add(a);
            auctionIterator.remove();
        }

        Transaction tx = new Transaction(buyer, Math.min(amount, total));
        tx.content.addAll(target);

        transactions.put(tx.id, tx);
        save();

        return tx;
    }

    public Transaction startTransactionWithRemovedAuction(LegalEntity buyer, Auction auction) {
        Transaction tx = new Transaction(buyer, auction.getAvailable());
        tx.content.add(auction);

        transactions.put(tx.id, tx);
        save();

        return tx;
    }
}
