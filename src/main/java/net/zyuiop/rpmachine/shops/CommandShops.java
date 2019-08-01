package net.zyuiop.rpmachine.shops;

import net.md_5.bungee.api.ChatColor;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.CompoundCommand;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.shops.types.ItemShopSign;
import net.zyuiop.rpmachine.shops.types.PlotSign;
import net.zyuiop.rpmachine.shops.types.TollShopSign;
import org.bukkit.entity.Player;

/**
 * @author zyuiop
 */
public class CommandShops extends CompoundCommand {
    public CommandShops() {
        super("shops", null, "boutiques", "shops");

        registerSubCommand("my", new SubCommand() {
            @Override
            public String getUsage() {
                return "";
            }

            @Override
            public String getDescription() {
                return "liste vos boutiques d'objets";
            }

            @Override
            public boolean run(Player player, String[] args) {
                for (ItemShopSign sign : RPMachine.getInstance().getShopsManager().getPlayerShops(player)) {
                    String typeLine = sign.getAction() == ItemShopSign.ShopAction.BUY ? ChatColor.RED + "Achat" : ChatColor.GREEN + "Vente";
                    String size = (sign.getAvailable() > sign.getAmountPerPackage() ? ChatColor.GREEN : ChatColor.RED) + "" + sign.getAvailable() + " en stock";
                    player.sendMessage(
                            ChatColor.GOLD + " -> " +
                                    sign.getLocation().getBlockX() + "-" + sign.getLocation().getBlockY() + "-" + sign.getLocation().getBlockZ() +
                                    " : " + typeLine + ChatColor.YELLOW + " de " + sign.getItemType() + " : " + size);
                }
                return true;
            }
        });

        registerBuilder(new ItemShopSign.Builder(), "shops", "boutiques", "boutiques", "shop", "boutique");
        registerBuilder(new PlotSign.Builder(), "plotshops", "vente de parcelle", "parcelles", "plot", "plots");
        registerBuilder(new TollShopSign.Builder(), "tolls", "péages", "peages", "péages", "tollshops", "péage", "peage", "toll");
    }

    private void registerBuilder(ShopBuilder builder, String name, String helpName, String... aliases) {
        registerSubCommand(name, new SubCommand() {
            @Override
            public String getUsage() {
                return "";
            }

            @Override
            public String getDescription() {
                return "affiche de l'aide pour les panneaux de " + helpName;
            }

            @Override
            public boolean run(Player sender, String[] args) {
                sender.sendMessage(ChatColor.GRAY + "Voici le format des panneaux de " + helpName + " :");
                builder.describeFormat(sender);
                return true;
            }
        }, aliases);
    }
}
