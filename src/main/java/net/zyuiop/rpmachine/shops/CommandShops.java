package net.zyuiop.rpmachine.shops;

import net.md_5.bungee.api.ChatColor;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.CompoundCommand;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.shops.types.EnchantingSign;
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
                player.sendMessage(ChatColor.YELLOW + " ---[Liste de vos boutiques]--- ");
                RPMachine.getInstance().getShopsManager().getPlayerShops(player).forEach(s -> player.sendMessage(ChatColor.GOLD + "- " + s.describe()));
                return true;
            }
        }, "list");

        registerBuilder(new ItemShopSign.Builder(), "shops", "boutiques", "boutiques");
        registerBuilder(new PlotSign.Builder(), "plotshops", "vente de parcelle", "parcelles", "plot", "plots");
        registerBuilder(new TollShopSign.Builder(), "tolls", "péages", "peages", "péages", "toll");
        registerBuilder(new EnchantingSign.Builder(), "enchant", "enchantement", "enchantement");
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
