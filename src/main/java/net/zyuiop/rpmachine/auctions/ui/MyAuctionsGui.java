package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.AuctionManager;
import net.zyuiop.rpmachine.auctions.SellOrder;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.utils.InventoryUtils;
import net.zyuiop.rpmachine.utils.MenuItem;
import net.zyuiop.rpmachine.utils.Symbols;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.List;

/**
 * @author Louis Vialar
 */
class MyAuctionsGui extends Window {
    private List<SellOrder> myAuctions;
    private ItemAuctionGui itemAuctionGui;
    private Material mat;

    MyAuctionsGui(ItemAuctionGui itemAuctionGui, Material mat, List<SellOrder> myAuctions) {
        super((int) (Math.ceil((myAuctions.size() + 3) / 9D) * 9D), "Mes enchères en cours", itemAuctionGui.getPlayer());
        this.myAuctions = myAuctions;
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
        myAuctions = AuctionManager.INSTANCE.getMyAuctions(mat, RPMachine.getPlayerActAs(player));

        clear();

        fill();
    }

    private void load() {
        for (int i = 0; i < (myAuctions.size()) && i < size - 3; ++i) {
            SellOrder a = myAuctions.get(i);

            setItem(i,
                    new MenuItem(mat, Math.min(mat.getMaxStackSize(), a.getAvailable()))
                            .setDescription(
                                    ChatColor.YELLOW + "Unités restantes : " + ChatColor.GOLD + a.getAvailable(),
                                    ChatColor.YELLOW + "Prix unitaire : " + ChatColor.GOLD + String.format("%.2f", a.getItemPrice()) + RPMachine.getCurrencyName(),
                                    ChatColor.GOLD + "",

                                    ChatColor.YELLOW + "Prix min actuel : " + ChatColor.GOLD + String.format("%.2f", AuctionManager.INSTANCE.minPrice(mat)) + RPMachine.getCurrencyName(),
                                    ChatColor.GOLD + "",
                                    ChatColor.YELLOW + Symbols.ARROW_RIGHT_FULL + " Clic : récupérer le lot"
                            )
                    , () -> {
                        int availableSize = InventoryUtils.availablePlaceFor(player.getInventory(), mat);

                        if (availableSize < a.getAvailable()) {
                            player.sendMessage(ChatColor.RED + "Pas assez de place ! Maximum dispo : " + availableSize + " items.");
                            refresh();
                            return;
                        }

                        if (AuctionManager.INSTANCE.removeAuction(a)) {
                            InventoryUtils.giveItems(mat, a.getAvailable(), player.getInventory());
                            player.sendMessage(ChatColor.GREEN + "Offre récupérée !");
                            refresh();
                        } else {
                            player.sendMessage(ChatColor.RED + "Cette offre n'est plus disponible.");
                            refresh();
                        }

                    });
        }
    }
}
