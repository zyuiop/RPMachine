package net.zyuiop.rpmachine.transportation;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.gui.Window;
import net.zyuiop.rpmachine.utils.MenuItem;
import net.zyuiop.rpmachine.utils.Messages;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class TransportationSelectGUI extends Window {
    private final List<TransportationPath> offeredRoutes;

    protected TransportationSelectGUI(Player player, List<TransportationPath> offeredRoutes) {
        super((int) (Math.ceil((offeredRoutes.size() + 3) / 9D) * 9D), ChatColor.DARK_AQUA + "Histori'air", player);
        this.offeredRoutes = offeredRoutes;
    }

    @Override
    public void fill() {
        var i = 0;
        for (var t : offeredRoutes) {
            super.setItem(i++,
                    new MenuItem(t.getIconMaterial())
                            .setName(t.getDisplayName())
                            .setDescription(
                                    ChatColor.DARK_PURPLE + "Cliquez ici pour commencer votre voyage",
                                    ChatColor.RED + "",
                                    ChatColor.YELLOW + "Coût : " + ChatColor.GOLD + String.format("%.2f", t.getPrice()) + RPMachine.getCurrencyName()),
                    () -> {
                        close();
                        var entity = RPMachine.database().getPlayerData(player);

                        if (t.getPrice() > 0) {
                            if (!entity.withdrawMoney(t.getPrice())) {
                                player.sendMessage(ChatColor.RED + "Vous n'avez pas assez d'argent pour payer ce trajet.");
                                return;
                            } else {
                                Messages.debit(player, t.getPrice(), "trajet Histori'air " + t.getDisplayName());
                            }
                        }

                        t.proceed(player);
                    });
        }
    }
}
