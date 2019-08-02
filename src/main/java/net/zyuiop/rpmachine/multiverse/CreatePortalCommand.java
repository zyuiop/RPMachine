package net.zyuiop.rpmachine.multiverse;

import net.zyuiop.rpmachine.commands.AbstractCommand;
import net.zyuiop.rpmachine.common.Area;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * @author Louis Vialar
 */
public class CreatePortalCommand extends AbstractCommand {
    private final MultiverseManager manager;

    protected CreatePortalCommand(MultiverseManager manager) {
        super("createportal", "admin.createportal");
        this.manager = manager;
    }

    @Override
    protected boolean onPlayerCommand(Player player, String command, String[] args) {
        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Utilisation : /" + command + " <cible>");
            return false;
        }

        MultiverseWorld world = manager.getWorld(player.getLocation().getWorld().getName());
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Vous n'êtes pas dans un multivers enregistré");
            return true;
        }

        Block block = player.getLocation().getBlock().getRelative(player.getFacing());
        if (block.getType() != Material.NETHER_PORTAL) {
            player.sendMessage(ChatColor.RED + "Vous devez vous trouver devant le portail pour utiliser cette commande.");
            return true;
        }

        Area portal = detectPortal(player.getFacing().getDirection(), block);
        MultiversePortal p = new MultiversePortal(portal, args[0]);
        manager.createPortal(p);
        player.sendMessage(ChatColor.GREEN + "Portal détecté (" + portal + ") et créé.");

        return true;
    }

    private int walkDirection(Block block, Vector direction, Vector playerAxix) {
        if (playerAxix.normalize().equals(direction))
            return (int) block.getLocation().toVector().dot(direction.multiply(direction));

        while (block.getType() == Material.NETHER_PORTAL) {
            block = block.getRelative(direction.getBlockX(), direction.getBlockY(), direction.getBlockZ());
        }

        return (int) block.getLocation().toVector().dot(direction.multiply(direction)); // Keep the frame by including it in the area
    }

    private Area detectPortal(Vector playerAxis, Block currentBlock) {
        int minX = walkDirection(currentBlock, new Vector(-1, 0, 0), playerAxis);
        int maxX = walkDirection(currentBlock, new Vector(1, 0, 0), playerAxis);
        int minY = walkDirection(currentBlock, new Vector(0, -1, 0), playerAxis);
        int maxY = walkDirection(currentBlock, new Vector(0, 1, 0), playerAxis);
        int minZ = walkDirection(currentBlock, new Vector(0, 0, -1), playerAxis);
        int maxZ = walkDirection(currentBlock, new Vector(0, 0, 1), playerAxis);

        return new Area(currentBlock.getWorld().getName(), minX, minY, minZ, maxX, maxY, maxZ);
    }
}
