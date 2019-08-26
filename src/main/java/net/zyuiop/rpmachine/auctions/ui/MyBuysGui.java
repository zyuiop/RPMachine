package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.AuctionManager;
import net.zyuiop.rpmachine.auctions.BuyOrder;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.utils.InventoryUtils;
import net.zyuiop.rpmachine.utils.MenuItem;
import net.zyuiop.rpmachine.utils.Messages;
import net.zyuiop.rpmachine.utils.Symbols;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.List;

/**
 * @author Louis Vialar
 */
class MyBuysGui extends Window {
    private List<BuyOrder> myAuctions;
    private ItemAuctionGui itemAuctionGui;
    private Material mat;

    MyBuysGui(ItemAuctionGui itemAuctionGui, Material mat, List<BuyOrder> myAuctions) {
        super((int) (Math.ceil((myAuctions.size() + 3) / 9D) * 9D), "Mes achats en cours", itemAuctionGui.getPlayer());
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
        myAuctions = AuctionManager.INSTANCE.getMyBuys(mat, RPMachine.getPlayerActAs(player));

        clear();

        fill();
    }

    private void load() {
        for (int i = 0; i < (myAuctions.size()) && i < size - 3; ++i) {
            BuyOrder a = myAuctions.get(i);

            double refund = a.getItemPrice() * a.getRemainingItems();

            setItem(i,
                    new MenuItem(mat, Math.max(1, Math.min(mat.getMaxStackSize(), a.getAvailable())))
                            .setDescription(
                                    ChatColor.YELLOW + "Quantité disponible : " + ChatColor.GOLD + a.getAvailable(),
                                    ChatColor.YELLOW + "Quantité totale à acheter : " + ChatColor.GOLD + a.getMaxBuy(),
                                    ChatColor.YELLOW + "Quantité restant à acheter : " + ChatColor.GOLD + a.getRemainingItems(),
                                    ChatColor.YELLOW + " ",
                                    ChatColor.YELLOW + Symbols.ARROW_RIGHT_FULL + " Clic : récupérer le lot",
                                    ChatColor.GRAY + "Votre offre sera retirée, vous obtiendrez vos items et " + String.format("%.4f", refund) + RPMachine.getCurrencyName()
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
                            double actualRefund = a.getItemPrice() * a.getRemainingItems();
                            RPMachine.getPlayerActAs(player).creditMoney(actualRefund);
                            Messages.credit(RPMachine.getPlayerActAs(player), actualRefund, "retrait d'une offre d'achat");
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
