package net.zyuiop.rpmachine.shops.types;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.auctions.AuctionManager;
import net.zyuiop.rpmachine.auctions.ItemAuctionGui;
import net.zyuiop.rpmachine.entities.AdminLegalEntity;
import net.zyuiop.rpmachine.entities.RoleToken;
import net.zyuiop.rpmachine.shops.ShopBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public class AuctionAccessSign extends AbstractShopSign {
    private Material itemType;

    public AuctionAccessSign(Location location) {
        super(location);
    }

    public void display() {
        doDisplay(true);
    }

    private void doDisplay(boolean first) {
        Block block = location.getLocation().getBlock();
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            if (itemType == null) {
                sign.setLine(0, ChatColor.AQUA + "HdV");
                sign.setLine(1, ChatColor.RED + "Non configuré");
                sign.setLine(3, ChatColor.RED + "<CLIC DROIT>");
            } else {
                sign.setLine(0, ChatColor.GOLD + "Hôtel des ventes");
                sign.setLine(1, ChatColor.BOLD + itemType.name());

                double min = AuctionManager.INSTANCE.minPrice(itemType);
                double avg = AuctionManager.INSTANCE.averagePrice(itemType);

                sign.setLine(2, ChatColor.BLUE + "Prix min: " + (min != min ? "N/A" : String.format("%.2f", min)));
                sign.setLine(3, ChatColor.BLUE + "Prix moy: " + (avg != avg ? "N/A" : String.format("%.2f", avg)));

                if (first)
                    Bukkit.getScheduler().runTaskTimer(RPMachine.getInstance(), () -> doDisplay(false), 20 * 60 * 5, 20 * 60 * 5); // Regular update
            }

            Bukkit.getScheduler().runTask(RPMachine.getInstance(), (Runnable) sign::update);
        } else {
            Bukkit.getLogger().info("Error : sign is not a sign, at " + location.toString());
        }

    }

    @Override
    public void breakSign() {
        super.breakSign();
    }

    void clickPrivileged(Player player, RoleToken tt, PlayerInteractEvent event) {
        if (itemType == null) {
            if (event.getItem() == null)
                return;

            itemType = event.getItem().getType();
            display();
        } else {
            clickUser(player, event);
        }
    }

    void clickUser(Player player, PlayerInteractEvent event) {
        new ItemAuctionGui(itemType, player).open();
    }

    @Override
    public void debug(Player p) {
        p.sendMessage(ChatColor.YELLOW + "-----[ Débug Shop ] -----");
        p.sendMessage(ChatColor.YELLOW + "Not a real shop :D");
    }

    @Override
    public String describe() {
        return "HDV " + itemType;
    }

    public static class Builder extends ShopBuilder<AuctionAccessSign> {
        @Override
        public void describeFormat(Player player) {
            player.sendMessage(ChatColor.YELLOW + " - [HDV]");
        }

        @Override
        public boolean hasPermission(RoleToken player) {
            return player.getLegalEntity() == AdminLegalEntity.INSTANCE;
        }

        public void postCreateInstructions(Player player) {
            player.sendMessage(ChatColor.AQUA + "[" + ChatColor.GREEN + "Shops" + ChatColor.AQUA + "] " + ChatColor.GREEN + "Votre HdV est presque prêt ! Cliquez droit avec un item pour l'initialiser.");
        }

        @Override
        public Optional<AuctionAccessSign> parseSign(Block block, RoleToken tt, String[] lines) throws SignPermissionError, SignParseError {
            return Optional.of(new AuctionAccessSign(block.getLocation()))
                    .map(sign -> {
                        sign.setOwner(tt.getTag());
                        sign.price = Double.MAX_VALUE;
                        return sign;
                    });
        }
    }
}
