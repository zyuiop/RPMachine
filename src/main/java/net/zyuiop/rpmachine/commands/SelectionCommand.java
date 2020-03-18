package net.zyuiop.rpmachine.commands;

import net.zyuiop.rpmachine.common.selections.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
public class SelectionCommand extends CompoundCommand {
    public SelectionCommand() {
        super("selection", null, "sel");


        registerSubCommand("multiple", new SubCommand() {
            @Override
            public String getUsage() {
                return "[reset|add|rectangle|polygon]";
            }

            @Override
            public String getDescription() {
                return "démarre une sélection multiple";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.RED + "Argument manquant.");
                    sender.sendMessage("- reset: (ré)initialise une rélection multiple");
                    sender.sendMessage("- add: confirme la sélection actuelle et l'ajoute à la sélection multiple");
                    sender.sendMessage("- rectangle: débute une nouvelle sélection rectangulaire à la sélection multiple");
                    sender.sendMessage("- polygon: débute une nouvelle sélection polygonale à la sélection multiple");
                    return true;
                }

                switch (args[0]) {
                    case "reset":
                        var cs = new CompoundSelection();
                        cs.resetSelection(new RectangleSelection());
                        PlayerSelection.createSelection(sender, cs);
                        sender.sendMessage(ChatColor.YELLOW + "Sélection initialisée, sélectionnez deux angles en cliquant gauche et droit avec un stick en main.");
                        sender.sendMessage(ChatColor.YELLOW + "Utilisez " + ChatColor.GOLD + "/sel add" + ChatColor.YELLOW + " lorsque vous avez terminé.");
                        break;

                    case "add":
                        var sel = PlayerSelection.getPlayerSelection(sender);
                        if (sel == null) {
                            sender.sendMessage(ChatColor.RED + "Aucune sélection en cours.");
                        } else if (sel instanceof CompoundSelection) {
                            if (((CompoundSelection) sel).addSelection()) {
                                sender.sendMessage(ChatColor.YELLOW + "Sélection actuelle ajoutée à la sélection multiple");
                            } else {
                                cs = new CompoundSelection();
                                cs.resetSelection(sel);
                                cs.addSelection();
                                PlayerSelection.createSelection(sender, cs);
                                sender.sendMessage(ChatColor.YELLOW + "Sélection actuelle ajountée à une nouvelle sélection multiple");
                            }
                        }
                        break;

                    case "rectangle":
                        sel = PlayerSelection.getPlayerSelection(sender);
                        if (sel instanceof CompoundSelection) {
                            ((CompoundSelection) sel).resetSelection(new RectangleSelection());
                        } else {
                            cs = new CompoundSelection();
                            cs.resetSelection(new RectangleSelection());
                            PlayerSelection.createSelection(sender, cs);
                        }
                        sender.sendMessage(ChatColor.YELLOW + "Sélection rectangulaire initialisée dans la sélection multiple, sélectionnez deux angles en cliquant gauche et droit avec un stick en main.");
                        sender.sendMessage(ChatColor.YELLOW + "Utilisez " + ChatColor.GOLD + "/sel add" + ChatColor.YELLOW + " lorsque vous avez terminé.");

                        break;

                    case "polygon":
                        sel = PlayerSelection.getPlayerSelection(sender);
                        if (sel instanceof CompoundSelection) {
                            ((CompoundSelection) sel).resetSelection(new PolygonSelection());
                        } else {
                            cs = new CompoundSelection();
                            cs.resetSelection(new PolygonSelection());
                            PlayerSelection.createSelection(sender, cs);
                        }
                        sender.sendMessage(ChatColor.YELLOW + "Sélection polygonale initialisée dans la sélection multiple, sélectionnez des points en utilisant un stick. Clic gauche sélectionne le premier point, clic droit sélectionne les points suivants. Minimum 3 points.");
                        sender.sendMessage(ChatColor.YELLOW + "Utilisez " + ChatColor.GOLD + "/sel add" + ChatColor.YELLOW + " lorsque vous avez terminé.");

                        break;

                    default:
                        sender.sendMessage(ChatColor.RED + "Argument incorrect.");
                        sender.sendMessage("- reset: (ré)initialise une rélection multiple");
                        sender.sendMessage("- add: confirme la sélection actuelle et l'ajoute à la sélection multiple");
                        sender.sendMessage("- rectangle: débute une nouvelle sélection rectangulaire à la sélection multiple");
                        sender.sendMessage("- polygon: débute une nouvelle sélection polygonale à la sélection multiple");
                }

                return true;
            }
        });

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
                    playerSelection.describe(sender);
                } else {
                    try {
                        int size = Integer.parseInt(args[0]);
                        playerSelection.expandY(size);

                        sender.sendMessage(ChatColor.GREEN + "Sélection étendue de " + size + " d'altitude.");
                        playerSelection.describe(sender);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Nombre invalide...");
                    }
                }
                return false;
            }
        });
    }
}
