package net.zyuiop.rpmachine.commands;

import net.zyuiop.rpmachine.common.selections.PlayerSelection;
import net.zyuiop.rpmachine.common.selections.PolygonSelection;
import net.zyuiop.rpmachine.common.selections.RectangleSelection;
import net.zyuiop.rpmachine.common.selections.Selection;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
public class SelectionCommand extends CompoundCommand {
    public SelectionCommand() {
        super("selection", null, "sel");

        registerSubCommand("rectangle", new SubCommand() {
            @Override
            public String getUsage() {
                return "";
            }

            @Override
            public String getDescription() {
                return "réinitialise la sélection et crée une sélection rectangulaire";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                PlayerSelection.createSelection(sender, new RectangleSelection());

                sender.sendMessage(ChatColor.YELLOW + "Sélection initialisée, sélectionnez deux angles en cliquant gauche et droit avec un stick en main.");
                return true;
            }
        });
        registerSubCommand("polygon", new SubCommand() {
            @Override
            public String getUsage() {
                return "";
            }

            @Override
            public String getDescription() {
                return "réinitialise la sélection et crée une sélection rectangulaire";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                PlayerSelection.createSelection(sender, new PolygonSelection());

                sender.sendMessage(ChatColor.YELLOW + "Sélection initialisée, sélectionnez des points en utilisant un stick. Clic gauche sélectionne le premier point, clic droit sélectionne les points suivants. Minimum 3 points.");
                return true;
            }
        });
        registerSubCommand("describe", new SubCommand() {
            @Override
            public String getUsage() {
                return "";
            }

            @Override
            public String getDescription() {
                return "décrit votre sélection actuelle";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                Selection playerSelection = PlayerSelection.getPlayerSelection(sender);
                if (playerSelection == null) {
                    sender.sendMessage(ChatColor.RED + "Aucune sélection en cours.");
                    return true;
                }

                playerSelection.describe(sender);
                return true;
            }
        });
        registerSubCommand("expand", new SubCommand() {
            @Override
            public String getUsage() {
                return "[y]";
            }

            @Override
            public String getDescription() {
                return "étend votre sélection en altitude, si y négatif la sélection sera étendue vers le bas, sinon vers le haut, et si absent sur toute l'altitude possible";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                Selection playerSelection = PlayerSelection.getPlayerSelection(sender);
                if (playerSelection == null) {
                    sender.sendMessage(ChatColor.RED + "Aucune sélection en cours.");
                    return true;
                }

                if (args.length == 0) {
                    playerSelection.expandY(-255);
                    playerSelection.expandY(255);

                    sender.sendMessage(ChatColor.GREEN + "Sélection étendue de la couche min à la couche max !");
                } else {
                    try {
                        int size = Integer.parseInt(args[0]);
                        playerSelection.expandY(size);

                        sender.sendMessage(ChatColor.GREEN + "Sélection étendue de " + size + " d'altitude.");
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Nombre invalide...");
                    }
                }
                return false;
            }
        });
    }
}
