package net.zyuiop.rpmachine.commands;

import net.zyuiop.rpmachine.auctions.AuctionManager;
import net.zyuiop.rpmachine.auctions.AuctionType;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * @author Louis Vialar
 */
public class AuctionTypesCommand extends CompoundCommand {
    public AuctionTypesCommand() {
        super("auctiontypes", "admin.auctiontypes", "at", "hdvt");

        registerSubCommand("list", new SubCommand() {
            @Override
            public String getUsage() {
                return "";
            }

            @Override
            public String getDescription() {
                return "liste les types d'HdV";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                sender.sendMessage(ChatColor.GRAY + "Configurations d'HdV :");
                AuctionManager.INSTANCE.getTypes().forEach((key, value) -> {
                    sender.sendMessage(ChatColor.GRAY + " - #" + ChatColor.GOLD + key + ChatColor.GRAY + " : " + ChatColor.YELLOW + value.getName());
                });
                return true;
            }
        });

        registerSubCommand("info", new SubCommand() {
            @Override
            public String getUsage() {
                return "<id>";
            }

            @Override
            public String getDescription() {
                return "affiche les items pris en charge par une configuration d'HdV";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                if (args.length < 1) {
                    sender.sendMessage(ChatColor.RED + "Id manquant");
                    return false;
                }

                AuctionType type = AuctionManager.INSTANCE.getTypes().get(args[0].toLowerCase());
                if (type == null) {
                    sender.sendMessage(ChatColor.RED + "Type introuvable " + args[0]);
                    return false;
                }

                sender.sendMessage(ChatColor.GRAY + "Configuration #" + ChatColor.GOLD + type.getId());
                sender.sendMessage(ChatColor.GRAY + "Nom : " + ChatColor.GOLD + type.getName());
                sender.sendMessage(ChatColor.GRAY + "Items pris en charge : ");
                type.getMaterials().forEach(v -> {
                    sender.sendMessage(ChatColor.GRAY + " - " + ChatColor.YELLOW + v.name());
                });
                return true;
            }
        });

        registerSubCommand("create", new SubCommand() {
            @Override
            public String getUsage() {
                return "<id> <name>";
            }

            @Override
            public String getDescription() {
                return "crée une configuration d'HdV";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Id et nom manquants");
                    return false;
                }

                String id = args[0];
                String name = StringUtils.join(args, " ", 1, args.length);

                if (name.length() > 34) {
                    sender.sendMessage(ChatColor.RED + "Nom trop long (max 34 caractères)");
                    return false;
                }

                AuctionType type = AuctionManager.INSTANCE.getType(id.toLowerCase());
                if (type != null) {
                    sender.sendMessage(ChatColor.RED + "Ce type existe déjà");
                    return false;
                }

                type = new AuctionType(id, name);
                AuctionManager.INSTANCE.createType(type);

                sender.sendMessage(ChatColor.GREEN + "Configuration #" + ChatColor.GOLD + type.getId() + ChatColor.GREEN + " créée !");
                sender.sendMessage(ChatColor.GRAY + "Ajuoutez des items avec " + ChatColor.YELLOW + "/" + command + " add " + type.getId() + " <type>");
                return true;
            }
        });

        registerSubCommand("add", new SubCommand() {
            @Override
            public String getUsage() {
                return "<id> <item type>";
            }

            @Override
            public String getDescription() {
                return "ajoute un objet à une configuration d'HdV";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Id et nom manquants");
                    return false;
                }

                String id = args[0];
                Material material = Material.getMaterial(args[1].toUpperCase());

                if (material == null) {
                    sender.sendMessage(ChatColor.RED + "Cet item n'existe pas");
                    return false;
                }

                AuctionType type = AuctionManager.INSTANCE.getType(id);
                if (type == null) {
                    sender.sendMessage(ChatColor.RED + "Ce type n'exsite pas");
                    return false;
                }

                type.getMaterials().add(material);
                AuctionManager.INSTANCE.save();

                sender.sendMessage(ChatColor.GREEN + "Item " + material + " ajouté à la configuration #" + ChatColor.GOLD + type.getId() + ChatColor.GREEN + " !");
                sender.sendMessage(ChatColor.GRAY + "Pour le retirer, utilisez " + ChatColor.YELLOW + "/" + command + " remove " + type.getId() + " " + args[1]);
                sender.sendMessage(ChatColor.GRAY + "Retirer un item n'entraine pas la suppression des enchères déjà existantes, il supprime simplement la porte d'accès.");
                return true;
            }
        });

        registerSubCommand("remove", new SubCommand() {
            @Override
            public String getUsage() {
                return "<id> <item type>";
            }

            @Override
            public String getDescription() {
                return "retire un objet d'une configuration d'HdV";
            }

            @Override
            public boolean run(Player sender, String command, String subCommand, String[] args) {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Id et nom manquants");
                    return false;
                }

                String id = args[0];
                Material material = Material.getMaterial(args[1].toUpperCase());

                if (material == null) {
                    sender.sendMessage(ChatColor.RED + "Cet item n'existe pas");
                    return false;
                }

                AuctionType type = AuctionManager.INSTANCE.getType(id);
                if (type == null) {
                    sender.sendMessage(ChatColor.RED + "Ce type n'exsite pas");
                    return false;
                }

                boolean success = type.getMaterials().remove(material);
                if (!success) {
                    sender.sendMessage(ChatColor.RED + "Cet objet n'était pas dans la configuration.");
                    return false;
                }

                AuctionManager.INSTANCE.save();
                sender.sendMessage(ChatColor.GREEN + "Item " + material + " retiré de la configuration #" + ChatColor.GOLD + type.getId() + ChatColor.GREEN + " !");
                return true;
            }
        });
    }
}
