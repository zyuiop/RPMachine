package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.AuctionManager;
import net.zyuiop.rpmachine.auctions.SellOrder;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.utils.MenuItem;
import net.zyuiop.rpmachine.utils.Symbols;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.TreeSet;

/**
 * @author Louis Vialar
 */
class AuctionsListGui extends Window {
    private TreeSet<SellOrder> auctions;
    private ItemAuctionGui itemAuctionGui;
    private Material mat;

    AuctionsListGui(ItemAuctionGui itemAuctionGui, Material mat, TreeSet<SellOrder> auctions) {
        super((int) (Math.min(6, Math.ceil((auctions.size() + 3) / 9D)) * 9D), "Enchères en cours", itemAuctionGui.getPlayer());
        this.auctions = auctions;
        this.itemAuctionGui = itemAuctionGui;
        this.mat = mat;
    }

    @Override
    public void fill() {
        setItem(size - 1, new MenuItem(Material.ARROW).setName(ChatColor.YELLOW + "Retour"), () -> {
            close();
            itemAuctionGui.open();
        });
        setItem(size - 2, new MenuItem(Material.WATER_BUCKET).setName(ChatColor.AQUA + "Raffraichir"), this::refresh);

        load();
    }

    private void refresh() {
        auctions = AuctionManager.INSTANCE.getAuctions(mat);
        clear();
        fill();
    }

    private void load() {
        int i = 0;

        for (SellOrder a : auctions) {
            if (i > size - 3)
                return;

            setItem(i,
                    new MenuItem(mat, Math.min(mat.getMaxStackSize(), a.getAvailable()))
                            .setDescription(
                                    ChatColor.YELLOW + "Quantité vendue : " + ChatColor.GOLD + a.getAvailable(),
                                    ChatColor.YELLOW + "Prix unitaire : " + ChatColor.GOLD + String.format("%.2f", a.getItemPrice()) + RPMachine.getCurrencyName(),
                                    ChatColor.YELLOW + "Vendeur : " + a.owner().shortDisplayable(),
                                    ChatColor.GOLD + "",

                                    ChatColor.YELLOW + "Prix du lot : " + ChatColor.GOLD + String.format("%.2f", (a.getAvailable() * a.getItemPrice())) + RPMachine.getCurrencyName(),
                                    ChatColor.GOLD + "",
                                    ChatColor.YELLOW + Symbols.ARROW_RIGHT_FULL + " Clic : acheter le lot"
                            )
                    , () -> {
                        if (AuctionManager.INSTANCE.removeAuction(a)) {
                            close();

                            new TransactionConfirmGui(player, mat, AuctionManager.INSTANCE.startBuyRemovedOrder(RPMachine.getPlayerActAs(player), a)).open();
                        } else {
                            player.sendMessage(ChatColor.RED + "Cette offre n'est plus disponible.");
                            refresh();
                        }
                    });

            ++i;
        }
    }
}
