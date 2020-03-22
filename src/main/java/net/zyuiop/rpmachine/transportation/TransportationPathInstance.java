package net.zyuiop.rpmachine.transportation;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.common.VirtualLocation;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class TransportationPathInstance extends BukkitRunnable {
    private static final Map<UUID, TransportationPathInstance> INSTANCES = new ConcurrentHashMap<>();

    private final Player player;
    private final TransportationPath path;

    private Mob carrier;
    private Location currentTarget;
    private Vector currentVector;
    private double prevDistance = 0D;
    private final Iterator<Location> iterator;

    private TransportationPathInstance(Player player, TransportationPath path) {
        this.player = player;
        this.path = path;

        this.currentTarget = path.getStartPoint().getLocation();

        List<Location> list = path.getLocations().stream().map(VirtualLocation::getLocation).collect(Collectors.toList());
        iterator  = list.iterator();
    }

    public static void startTransportation(Player player, TransportationPath path) {
        if (INSTANCES.containsKey(player.getUniqueId())) {
            INSTANCES.remove(player.getUniqueId()).cancel();
        }

        var i = new TransportationPathInstance(player, path);
        INSTANCES.put(player.getUniqueId(), i);
        i.start();
    }

    public static TransportationPathInstance getCurrentTransportation(Player player) {
        return INSTANCES.get(player.getUniqueId());
    }

    private void start() {
        carrier = (Mob) currentTarget.getWorld().spawnEntity(currentTarget, path.getType());

        carrier.addPassenger(player);
        carrier.clearLootTable();
        carrier.setInvulnerable(true);
        carrier.setAI(false);
        carrier.setAware(false);
        carrier.setTarget(null);
        carrier.setCustomNameVisible(true);

        carrier.setCustomName(ChatColor.DARK_AQUA + "Hystori'air");

        carrier.setCollidable(false);

        runTaskTimer(RPMachine.getInstance(), 2, 2);
    }

    @Override
    public void run() {
        if (currentVector == null) {
            next();
        }

        var dist = carrier.getLocation().distanceSquared(currentTarget);
        if (dist <= 1.0 || dist > prevDistance) {
            next();
        } else {
            prevDistance = dist;
        }

        carrier.setVelocity(currentVector);
    }

    private void next() {
        if (!iterator.hasNext()) {
            player.sendMessage(ChatColor.GREEN + "Merci d'avoir voyagé avec Histori'air !");
            cancel();
        } else {
            var oldTarget = carrier.getLocation();
            currentTarget = iterator.next();
            currentVector = currentTarget.toVector().subtract(oldTarget.toVector()).normalize().multiply(0.5);
            prevDistance = oldTarget.distanceSquared(currentTarget) + 10;


            var loc = carrier.getLocation().clone().setDirection(currentVector);
            carrier.setRotation(loc.getYaw(), loc.getPitch());
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        INSTANCES.remove(player.getUniqueId());
        carrier.remove();
        super.cancel();
    }
}
