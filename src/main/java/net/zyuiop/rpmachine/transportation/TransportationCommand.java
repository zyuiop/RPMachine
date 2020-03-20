package net.zyuiop.rpmachine.transportation;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.commands.CompoundCommand;
import net.zyuiop.rpmachine.commands.SubCommand;
import net.zyuiop.rpmachine.common.VirtualLocation;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TransportationCommand extends CompoundCommand {
    private final Map<UUID, TransportationPath> workingPaths = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> previewing = new ConcurrentHashMap<>();

    public TransportationCommand(TransportationManager manager) {
        super("transportation", "transportation.manage");

        registerSubCommand("start", new SubCommand() {
            @Override
            public String getUsage() {
                return "[nom interne] [nom public]";
            }

            @Override
            public String getDescription() {
                return "commence un nouvel itinéraire de transport à la position actuelle";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Utilisation : /" + command + " " + subCommand + " " + getUsage());
                    return true;
                }

                var p = new TransportationPath();
                p.setStartPoint(new VirtualLocation(sender.getLocation()));
                p.setName(args[0]);
                p.setDisplayName(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                p.setIconMaterial(Material.STONE);
                p.setPrice(0.0D);
                p.setType(EntityType.PHANTOM);

                manager.addPath(p);
                workingPaths.put(sender.getUniqueId(), p);

                sender.sendMessage(ChatColor.GREEN + "Itinéraire lancé et position de départ définie. Utilisez /transportation add pour ajouter des points.");
                return true;
            }
        });

        registerSubCommand("add", new SubCommand() {
            @Override
            public String getUsage() {
                return "";
            }

            @Override
            public String getDescription() {
                return "ajoute un point à l'itinéraire de transport";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                var p = workingPaths.get(sender.getUniqueId());
                if (p == null) {
                    sender.sendMessage(ChatColor.RED + "Aucun chemin en cours d'édition");
                    return true;
                }

                p.getLocations().add(new VirtualLocation(sender.getLocation()));
                manager.savePath(p);
                sender.sendMessage(ChatColor.GREEN + "Position #" + p.getLocations().size() + " ajoutée, utilisez /transportation remove pour supprimer le dernier point.");
                return true;
            }
        });
        registerSubCommand("setstart", new SubCommand() {
            @Override
            public String getUsage() {
                return "";
            }

            @Override
            public String getDescription() {
                return "change le départ de l'itinéraire de transport";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                var p = workingPaths.get(sender.getUniqueId());
                if (p == null) {
                    sender.sendMessage(ChatColor.RED + "Aucun chemin en cours d'édition");
                    return true;
                }

                p.setStartPoint(new VirtualLocation(sender.getLocation()));
                manager.savePath(p);
                sender.sendMessage(ChatColor.GREEN + "Point de départ modifié.");
                return true;
            }
        }, "ss");

        registerSubCommand("remove", new SubCommand() {
            @Override
            public String getUsage() {
                return "";
            }

            @Override
            public String getDescription() {
                return "supprime la dernière position de l'itinéraire";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                var p = workingPaths.get(sender.getUniqueId());
                if (p == null) {
                    sender.sendMessage(ChatColor.RED + "Aucun chemin en cours d'édition");
                    return true;
                }

                var loc = p.getLocations();
                if (loc.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "Aucune position dans le chemin");
                } else {
                    loc.remove(loc.size() - 1);
                    manager.savePath(p);
                    sender.sendMessage(ChatColor.GREEN + "Position #" + (loc.size() + 1) + " supprimée.");
                }
                return true;
            }
        });

        registerSubCommand("price", new SubCommand() {
            @Override
            public String getUsage() {
                return "[prix]";
            }

            @Override
            public String getDescription() {
                return "change le prix d'utilisation de l'itinéraire";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                var p = workingPaths.get(sender.getUniqueId());
                if (p == null) {
                    sender.sendMessage(ChatColor.RED + "Aucun chemin en cours d'édition");
                    return true;
                }

                if (args.length < 1) {
                    sender.sendMessage(ChatColor.RED + "Utilisation : /" + command + " " + subCommand + " " + getUsage());
                    return true;
                }

                p.setPrice(Double.parseDouble(args[0]));
                manager.savePath(p);
                sender.sendMessage(ChatColor.GREEN + "Prix défini.");
                return true;
            }
        });

        registerSubCommand("entitytype", new SubCommand() {
            @Override
            public String getUsage() {
                return "[type d'entité]";
            }

            @Override
            public String getDescription() {
                return "change le type de la monture";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                var p = workingPaths.get(sender.getUniqueId());
                if (p == null) {
                    sender.sendMessage(ChatColor.RED + "Aucun chemin en cours d'édition");
                    return true;
                }

                if (args.length < 1) {
                    sender.sendMessage(ChatColor.RED + "Utilisation : /" + command + " " + subCommand + " " + getUsage());
                    return true;
                }

                try {
                    var t = EntityType.valueOf(args[0]);

                    if (t.getEntityClass() != null && Mob.class.isAssignableFrom(t.getEntityClass())) {
                        p.setType(t);
                        manager.savePath(p);
                        sender.sendMessage(ChatColor.GREEN + "Monture définie.");
                    } else {
                        sender.sendMessage(ChatColor.RED + "La monture doit impérativement être un mob.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    sender.sendMessage(ChatColor.RED + "Type d'entité non trouvé.");
                }
                return true;
            }
        }, "et");

        registerSubCommand("item", new SubCommand() {
            @Override
            public String getUsage() {
                return "[item]";
            }

            @Override
            public String getDescription() {
                return "change l'icone de l'itinéraire";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                var p = workingPaths.get(sender.getUniqueId());
                if (p == null) {
                    sender.sendMessage(ChatColor.RED + "Aucun chemin en cours d'édition");
                    return true;
                }

                if (args.length < 1) {
                    sender.sendMessage(ChatColor.RED + "Utilisation : /" + command + " " + subCommand + " " + getUsage());
                    return true;
                }

                var t = Material.getMaterial(args[0]);

                if (t == null) {
                    sender.sendMessage(ChatColor.RED + "Matériau introuvable.");
                } else {
                    p.setIconMaterial(t);
                    manager.savePath(p);
                    sender.sendMessage(ChatColor.GREEN + "Icone définie.");
                }

                return true;
            }
        });

        registerSubCommand("preview", new SubCommand() {
            @Override
            public String getUsage() {
                return "";
            }

            @Override
            public String getDescription() {
                return "active/désactive le preview";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                if (previewing.containsKey(sender.getUniqueId())) {
                    sender.sendMessage(ChatColor.GREEN + "Preview désactivé");
                    previewing.remove(sender.getUniqueId()).cancel();
                    return true;
                }

                var r = new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!sender.isOnline()) {
                            cancel();
                        } else if (!workingPaths.containsKey(sender.getUniqueId())) {
                            cancel();
                            sender.sendMessage(ChatColor.GREEN + "Preview désactivé (aucun chemin)");
                        } else {
                            var path = workingPaths.get(sender.getUniqueId());
                            path.display(sender);
                        }
                    }
                };

                previewing.put(sender.getUniqueId(), r.runTaskTimer(RPMachine.getInstance(),0, 40));
                sender.sendMessage(ChatColor.GREEN + "Preview activé");


                return true;
            }
        });

        registerSubCommand("destroy", new SubCommand() {
            @Override
            public String getUsage() {
                return "";
            }

            @Override
            public String getDescription() {
                return "supprime l'itinéraire en cours d'édition";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                var p = workingPaths.get(sender.getUniqueId());
                if (p == null) {
                    sender.sendMessage(ChatColor.RED + "Aucun chemin en cours d'édition");
                    return true;
                }

                manager.deletePath(p);
                sender.sendMessage(ChatColor.GREEN + "Itinéraire supprimé.");
                return true;
            }
        });

        registerSubCommand("load", new SubCommand() {
            @Override
            public String getUsage() {
                return "[path]";
            }

            @Override
            public String getDescription() {
                return "charge un itinéraire pour le modifier";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                if (args.length < 1) {
                    sender.sendMessage(ChatColor.RED + "Utilisation : /" + command + " " + subCommand + " " + getUsage());
                    return true;
                }

                var path = manager.getPath(args[0]);

                if (path == null) {
                    sender.sendMessage(ChatColor.RED + "Chemin introuvable.");
                    return true;
                }

                workingPaths.put(sender.getUniqueId(), path);
                sender.sendMessage(ChatColor.GREEN + "Chemin sélectionné pour l'édition.");
                return true;
            }
        });

        registerSubCommand("list", new SubCommand() {
            @Override
            public String getUsage() {
                return "";
            }

            @Override
            public String getDescription() {
                return "liste les itinéraires";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                sender.sendMessage(ChatColor.YELLOW + "Liste des chemins disponibles :");

                manager.getPaths().forEach((name, path) -> {
                    sender.sendMessage(ChatColor.YELLOW + " - " + ChatColor.GOLD + name + ChatColor.YELLOW + " : " + path.getDisplayName());
                });
                return true;
            }
        });
    }
}
