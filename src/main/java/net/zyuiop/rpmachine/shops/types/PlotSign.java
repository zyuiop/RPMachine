package net.zyuiop.rpmachine.shops.types;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.entities.RoleToken;
import net.zyuiop.rpmachine.permissions.PlotPermissions;
import net.zyuiop.rpmachine.permissions.ShopPermissions;
import net.zyuiop.rpmachine.reflection.ReflectionUtils;
import net.zyuiop.rpmachine.shops.ShopBuilder;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;


public class PlotSign extends AbstractShopSign {
    protected String plotName;
    protected String cityName;
    protected boolean citizensOnly;

    public PlotSign() {
        super();
    }

    public PlotSign(Location location) {
        super(location);
    }

    public static void launchfw(final Location loc, final FireworkEffect effect) {
        ReflectionUtils.getVersion().launchfw(loc, effect);
    }

    public String getPlotName() {
        return plotName;
    }

    public void setPlotName(String plotName) {
        this.plotName = plotName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public boolean isCitizensOnly() {
        return citizensOnly;
    }

    public void setCitizensOnly(boolean citizensOnly) {
        this.citizensOnly = citizensOnly;
    }

    public void display() {
        Block block = location.getLocation().getBlock();
        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            sign.setLine(0, ChatColor.GREEN + "[Terrain]");
            sign.setLine(1, ChatColor.BLUE + "Prix : " + price);
            if (citizensOnly) {
                sign.setLine(2, ChatColor.RED + "Citoyens");
            } else {
                sign.setLine(2, ChatColor.GREEN + "Public");
            }
            sign.setLine(3, ChatColor.GOLD + "> Acheter <");

            Bukkit.getScheduler().runTask(RPMachine.getInstance(), (Runnable) sign::update);
        } else {
            Bukkit.getLogger().info("Error : sign is not a sign, at " + location.toString());
        }
    }

    @Override
    public String describe() {
        return super.describe() + ChatColor.DARK_GREEN + "Parcelle" + ChatColor.YELLOW + " " + plotName + " dans " + cityName + " pour " + ChatColor.AQUA + price + RPMachine.getCurrencyName();
    }

    @Override
    void clickPrivileged(Player player, RoleToken token, PlayerInteractEvent event) {
        clickUser(player, event);
    }

    void clickUser(Player player, PlayerInteractEvent event) {
        City city = RPMachine.getInstance().getCitiesManager().getCity(cityName);
        if (city == null) {
            player.sendMessage(ChatColor.RED + "Une erreur s'est produite : la ville n'existe pas.");
            return;
        }

        Plot plot = city.getPlots().get(plotName);
        if (plot == null) {
            player.sendMessage(ChatColor.RED + "Une erreur s'est produite : la parcelle n'existe pas.");
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            player.sendMessage(ChatColor.GOLD + "-----[ Informations Parcelle ]-----");
            player.sendMessage(ChatColor.YELLOW + "Nom : " + plot.getPlotName());
            player.sendMessage(ChatColor.YELLOW + "Ville : " + city.getCityName());
            player.sendMessage(ChatColor.YELLOW + "Surface : " + plot.getArea().getSquareArea() + " blocs²");
            player.sendMessage(ChatColor.YELLOW + "Volume : " + plot.getArea().getVolume() + " blocs³");
            player.sendMessage(ChatColor.YELLOW + "Impots : " + plot.getArea().getSquareArea() * city.getTaxes() + " $");
            return;
        }

        if (citizensOnly && !city.getInhabitants().contains(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Vous n'êtes pas citoyen de cette ville.");
            return;
        }

        if (plot.ownerTag() != null && ownerTag() != null && !ownerTag().equals(plot.ownerTag())) {
            // Un joueur peut pas vendre une parcelle random
            breakSign(null);
            player.sendMessage(ChatColor.RED + "Erreur : cette parcelle n'appartient plus à " + owner().shortDisplayable());
            Bukkit.getLogger().info("Old plot sign found : " + plot.getOwner() + " / sign " + ownerTag());
            return;
        }

        RoleToken tt = RPMachine.getPlayerRoleToken(player);

        if (!tt.checkDelegatedPermission(ShopPermissions.BUY_PLOTS))
            return;

        LegalEntity data = tt.getLegalEntity();
        if (data.withdrawMoney(price)) {
            Messages.debitEntity(player, data, price, "achat de parcelle");

            if (plot.getOwner() == null) {
                city.creditMoney(price);

                Messages.credit(city, price, "vente de parcelle");
            } else {
                // On crédite à l'owner du panneau
                // TODO: make tax customizable
                Messages.credit(city, price * 0.2D, "taxe sur vente de parcelle");
                Messages.credit(owner(), price * 0.8D, "vente de parcelle");

                owner().creditMoney(price * 0.8D);
                city.creditMoney(price * 0.2D);
            }

            plot.setOwner(data);
            plot.setPlotMembers(new CopyOnWriteArrayList<>());

            city.getPlots().put(plotName, plot);
            RPMachine.getInstance().getCitiesManager().saveCity(city);
            Bukkit.getScheduler().runTask(RPMachine.getInstance(), () -> {
                breakSign();
                launchfw(location.getLocation(), FireworkEffect.builder().withColor(Color.WHITE, Color.GRAY, Color.BLACK).with(FireworkEffect.Type.STAR).build());
            });
            player.sendMessage(ChatColor.GREEN + "Vous êtes désormais propriétaire de cette parcelle.");
        } else {
            Messages.notEnoughMoneyEntity(player, data, price);
        }
    }

    @Override
    public void debug(Player p) {
        p.sendMessage(ChatColor.YELLOW + "-----[ Débug Shop ] -----");
        p.sendMessage(ChatColor.YELLOW + "Price : " + getPrice());
        p.sendMessage(ChatColor.YELLOW + "Owner (Tag/displayable) : " + ownerTag() + " / " + owner().displayable());
        p.sendMessage(ChatColor.YELLOW + "Parcelle : " + getPlotName());
        p.sendMessage(ChatColor.YELLOW + "Ville : " + getCityName());
        p.sendMessage(ChatColor.YELLOW + "Citizens Only : " + isCitizensOnly());
    }

    public static class Builder extends ShopBuilder<PlotSign> {
        @Override
        public void describeFormat(Player player) {
            player.sendMessage(ChatColor.YELLOW + " - PlotShop");
            player.sendMessage(ChatColor.AQUA + " - Prix de la parcelle");
            player.sendMessage(ChatColor.AQUA + " - <all|citizens> (limite qui peut acheter la parcelle)");
        }

        @Override
        public boolean hasPermission(RoleToken player) {
            return player.hasDelegatedPermission(ShopPermissions.CREATE_PLOT_SHOPS);
        }

        @Override
        public Optional<PlotSign> parseSign(Block block, RoleToken tt, String[] lines) throws SignPermissionError, SignParseError {
            return Optional.of(new PlotSign(block.getLocation()))
                    .map(sign -> {
                        City city = RPMachine.getInstance().getCitiesManager().getCityHere(block.getChunk());
                        if (city != null) {
                            Plot plot = city.getPlotHere(sign.getLocation());
                            if (plot == null) {
                                throw new SignParseError("Le panneau ne se trouve pas dans une parcelle");
                            }

                            if (!plot.getOwner().equals(tt.getTag())) {
                                throw new SignParseError("Vous n'êtes pas propriétaire de cette parcelle");
                            } else if (!tt.hasDelegatedPermission(PlotPermissions.SELL_PLOT)) {
                                throw new SignPermissionError("Vous ne pouvez pas vendre cette parcelle");
                            }

                            sign.plotName = plot.getPlotName();
                            sign.cityName = city.getCityName();

                            return sign;
                        } else {
                            throw new SignParseError("Le panneau ne se trouve pas dans une ville.");
                        }
                    })
                    .flatMap(sign -> extractPrice(lines[1]).map(price -> {
                        sign.price = price;
                        return sign;
                    }))
                    .map(sign -> {
                        sign.citizensOnly = lines[2] != null && lines[2].equalsIgnoreCase("citziens");
                        sign.setOwner(tt.getTag());

                        return sign;
                    });
        }
    }
}
