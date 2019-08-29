package net.zyuiop.rpmachine.auctions.ui;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.AuctionManager;
import net.zyuiop.rpmachine.entities.AdminLegalEntity;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.permissions.ShopPermissions;
import net.zyuiop.rpmachine.utils.MenuItem;
import net.zyuiop.rpmachine.utils.Symbols;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
public class ItemAuctionGui extends Window {
    private final Material mat;

    public ItemAuctionGui(Material material, Player player) {
        super(9, "Hôtel Des Ventes " + material, player);
        this.mat = material;
    }

    @Override
    public void fill() {
        double minPrice = AuctionManager.INSTANCE.minPrice(mat);
        double avgPrice = AuctionManager.INSTANCE.averagePrice(mat);

        int avail = AuctionManager.INSTANCE.countAvailable(mat);
        String desc =
                ChatColor.YELLOW + "Prix moyen: " + (avgPrice != avgPrice ? ChatColor.RED + "Inconnu" : ChatColor.AQUA + String.format("%.2f", avgPrice)) + RPMachine.getCurrencyName() + "\n" +
                        ChatColor.YELLOW + "Prix minimum: " + (minPrice != minPrice ? ChatColor.RED + "Inconnu" : ChatColor.AQUA + String.format("%.2f", minPrice)) + RPMachine.getCurrencyName() + "\n" +
                        ChatColor.YELLOW + "Disponible: " + avail;

        setItem(4, new MenuItem(mat).setName(mat.name()).setDescriptionBlock(desc +
                "\n" + ChatColor.GREEN +
                "\n" + ChatColor.YELLOW + Symbols.ARROW_RIGHT_FULL + "Clic : voir le détail des offres (achat et vente)"
        ), () -> {

            close();
            new OrdersGui(this, getPlayer(), mat).open();
        });

        LegalEntity token = RPMachine.getPlayerActAs(player);

        if (token.hasDelegatedPermission(player, ShopPermissions.BUY_ITEMS)) {
            setItem(0, new MenuItem(Material.GOLD_INGOT).setName("Acheter automatiquement").setDescriptionBlock(desc), () -> {
                close();
                new BuyGui(player, mat, true).open();
            });

            setItem(1, new MenuItem(Material.GOLD_BLOCK).setName("Placer offre d'achat").setDescription(ChatColor.YELLOW + "Indiquez votre offre d'achat et payez immédiatement", ChatColor.YELLOW + "Collectez les items au fur et à mesure"), () -> {
                close();
                new BuyGui(player, mat, false).open();
            });
        }


        if (token.hasDelegatedPermission(player, ShopPermissions.SELL_ITEMS)) {
            setItem(7, new MenuItem(Material.CHEST).setName("Placer offre de vente").setDescriptionBlock(desc), () -> {
                close();
                new SellSetPriceGui(player, mat, avgPrice, minPrice).open();
            });

            setItem(8, new MenuItem(Material.ENDER_CHEST).setName("Vendre automatiquement").setDescription(ChatColor.YELLOW + "Vendez directement aux offres d'achat placées par d'autres joueurs", ChatColor.RED + "Vous ne choissez pas le prix, mais pouvez le refuser si trop bas.", ChatColor.GREEN + "Le produit de la vente est crédité immédiatement"), () -> {
                close();
                new AutoSellAddItemsGui(player, mat).open();
            });
        }

        if (token.hasDelegatedPermission(player, ShopPermissions.GET_SHOP_STOCK)) {
            setItem(6, new MenuItem(Material.RED_SHULKER_BOX).setName("Mes ventes").setDescription(ChatColor.YELLOW + "Pour lister et retirer certaines de vos ventes en cours"), () -> {
                close();
                new MySellOrdersGui(this, getPlayer(), mat).open();
            });

            setItem(2, new MenuItem(Material.GREEN_SHULKER_BOX).setName("Mes achats").setDescription(ChatColor.YELLOW + "Pour lister et retirer certaines de vos offres d'achat en cours"), () -> {
                close();
                new MyBuyOrdersGui(this, getPlayer(), mat).open();
            });
        }
    }

    @Override
    public void open() {
        if (RPMachine.getPlayerActAs(player) == AdminLegalEntity.INSTANCE) {
            player.sendMessage(ChatColor.RED + "La Confédération ne peut pas utiliser l'hôtel des ventes.");
            return;
        }

        super.open();
    }
}
