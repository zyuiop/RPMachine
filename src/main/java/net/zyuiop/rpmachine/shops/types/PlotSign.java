package net.zyuiop.rpmachine.shops.types;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.City;
import net.zyuiop.rpmachine.common.Plot;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.entities.RoleToken;
import net.zyuiop.rpmachine.json.JsonExclude;
import net.zyuiop.rpmachine.permissions.PlotPermissions;
import net.zyuiop.rpmachine.permissions.ShopPermissions;
import net.zyuiop.rpmachine.projects.Project;
import net.zyuiop.rpmachine.reflection.ReflectionUtils;
import net.zyuiop.rpmachine.shops.ShopBuilder;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;


public class PlotSign extends AbstractShopSign {
    protected String plotName;
    protected String cityName;
    protected boolean citizensOnly;

    @JsonExclude
    private Map<UUID, Long> confirmations = new HashMap<>();

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

    public String getCityName() {
        return cityName;
    }

    public boolean isCitizensOnly() {
        return cityName != null && citizensOnly;
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
        if (cityName != null)
            return super.describe() + ChatColor.DARK_GREEN + "Parcelle" + ChatColor.YELLOW + " " + plotName + " dans " + cityName + " pour " + ChatColor.AQUA + price + RPMachine.getCurrencyName();
        return super.describe() + ChatColor.DARK_GREEN + "Projet" + ChatColor.GOLD + " " + plotName + " pour " + ChatColor.AQUA + price + RPMachine.getCurrencyName();
    }

    @Override
    void clickPrivileged(Player player, RoleToken token, PlayerInteractEvent event) {
        clickUser(player, event);
    }

    void clickUser(Player player, PlayerInteractEvent event) {
        Plot plot = null;
        City city = null;
        String cName = null;
        double taxes = 0D;
        double sellTaxes = 0D;

        if (cityName == null) {
            Project p = RPMachine.getInstance().getProjectsManager().getZone(plotName);
            cName = "Aucune";
            taxes = RPMachine.getInstance().getProjectsManager().getGlobalTax();
            sellTaxes = RPMachine.getInstance().getProjectsManager().getGlobalSaleTax();
            plot = p;
        } else {
            city = RPMachine.getInstance().getCitiesManager().getCity(cityName);
            if (city == null) {
                player.sendMessage(ChatColor.RED + "Une erreur s'est produite : la ville n'existe pas.");
                return;
            }

            taxes = city.getTaxes();
            sellTaxes = city.getPlotSellTaxRate();
            cName = city.getCityName();

            plot = city.getPlot(plotName);
        }

        if (plot == null) {
            player.sendMessage(ChatColor.RED + "Une erreur s'est produite : la parcelle n'existe pas.");
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            player.sendMessage(ChatColor.GOLD + "-----[ Informations Parcelle ]-----");
            player.sendMessage(ChatColor.YELLOW + "Nom : " + plot.getPlotName());
            player.sendMessage(ChatColor.YELLOW + "Ville : " + cName);
            player.sendMessage(ChatColor.YELLOW + "Surface : " + plot.getArea().computeArea() + " blocs²");
            player.sendMessage(ChatColor.YELLOW + "Volume : " + plot.getArea().computeVolume() + " blocs³");
            player.sendMessage(ChatColor.YELLOW + "Impots : " + plot.getArea().computeArea() * taxes + " $");
            player.sendMessage(ChatColor.GRAY + "Pour plus d'informations, utilisez" + ChatColor.YELLOW + " /plot info");
            return;
        }

        if (citizensOnly && city != null && !city.getInhabitants().contains(player.getUniqueId())) {
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

        var confirm = confirmations.get(player.getUniqueId());
        if (confirm == null || confirm < System.currentTimeMillis()) {
            player.sendMessage(ChatColor.YELLOW + "Êtes vous sûr(e) de vouloir acheter cette parcelle pour " + ChatColor.GOLD + String.format("%.2f", price) + RPMachine.getCurrencyName() + ChatColor.YELLOW + " ?");
            player.sendMessage(ChatColor.GRAY + "Pour confirmer l'achat, " + ChatColor.YELLOW + "cliquez à nouveau dans les 30 prochaines secondes" + ChatColor.GRAY + " !");
            confirmations.put(player.getUniqueId(), System.currentTimeMillis() + 40 * 60 * 1000L); // Add 40secs because people
            return;
        }

        LegalEntity data = tt.getLegalEntity();
        if (data.withdrawMoney(price)) {
            Messages.debitEntity(player, data, price, "achat de parcelle");

            if (plot.getOwner() == null) {
                if (city != null) {
                    city.creditMoney(price);
                    Messages.credit(city, price, "vente de parcelle à " + data.displayable());
                }
            } else {
                // On crédite à l'owner du panneau
                double userRate = 1 - sellTaxes;

                if (city != null) {
                    Messages.credit(city, price * sellTaxes, "taxe sur vente de parcelle");
                    city.creditMoney(price * sellTaxes);
                }

                Messages.credit(owner(), price * userRate, "vente de parcelle à " + data.displayable());
                owner().creditMoney(price * userRate);
            }

            plot.setOwner(data);
            plot.setPlotMembers(new CopyOnWriteArrayList<>());
            if (city != null)
                city.save();

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
                            } else if (plot.isDueForDeletion()) {
                                throw new SignPermissionError("Vous ne pouvez pas vendre une parcelle qui est en cours de suppression");
                            }

                            if (plot.getOwner() != null && !plot.getOwner().equals(tt.getTag())) {
                                throw new SignParseError("Vous n'êtes pas propriétaire de cette parcelle");
                            } else if (!tt.hasDelegatedPermission(PlotPermissions.SELL_PLOT)) {
                                throw new SignPermissionError("Vous ne pouvez pas vendre cette parcelle");
                            }

                            sign.plotName = plot.getPlotName();
                            sign.cityName = city.getCityName();

                            return sign;
                        } else {
                            Project project = RPMachine.getInstance().getProjectsManager().getZoneHere(block.getLocation());
                            if (project == null) {
                                throw new SignParseError("Le panneau ne se trouve pas dans une ville ou un projet");
                            }

                            if (project.getOwner() != null && !project.getOwner().equals(tt.getTag())) {
                                throw new SignParseError("Vous n'êtes pas propriétaire de cette parcelle");
                            } else if (!tt.hasDelegatedPermission(PlotPermissions.SELL_PLOT)) {
                                throw new SignPermissionError("Vous ne pouvez pas vendre cette parcelle");
                            }

                            sign.plotName = project.getPlotName();
                            sign.cityName = null;

                            return sign;
                        }
                    })
                    .flatMap(sign -> extractPrice(lines[1]).map(price -> {
                        sign.price = price;
                        return sign;
                    }))
                    .map(sign -> {
                        sign.citizensOnly = sign.cityName != null && lines[2] != null && lines[2].equalsIgnoreCase("citizens");
                        sign.setOwner(tt.getTag());

                        return sign;
                    });
        }
    }
}
