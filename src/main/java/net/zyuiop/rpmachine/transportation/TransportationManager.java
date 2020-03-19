package net.zyuiop.rpmachine.transportation;

import net.zyuiop.rpmachine.database.filestorage.FileEntityStore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.*;

public class TransportationManager extends FileEntityStore<TransportationPath> {
    private Map<String, TransportationPath> paths = new HashMap<>();
    private List<TransportationNPC> NPCs = new ArrayList<>();

    public TransportationManager() {
        super(TransportationPath.class, "transportation");

        // Here, kill all remaining invuln. entities
        Bukkit.getLogger().info("Clearing invulnerable entities...");
        Bukkit.getWorlds().forEach(world -> {
            for (var e: world.getLivingEntities()) {
                Bukkit.getLogger().info(e.getType() + " - " + e.isInvulnerable() + " - " + e.getCustomName());

                if (e.isInvulnerable()) e.remove();
            }
        });
        Bukkit.getLogger().info("Loading transportation paths...");
        load();
    }

    public void addPath(TransportationPath path) {
        super.createEntity(path.getName(), path);
    }

    public void savePath(TransportationPath path) {
        super.saveEntity(path);
    }

    public void deletePath(TransportationPath path) {
        super.removeEntity(path);

        paths.remove(path.getName().toLowerCase());

        var availableLoc = NPCs.stream().filter(npc -> npc.getLocation().distanceSquared(path.getStartPoint().getLocation()) <= 16).findFirst();

        if (availableLoc.isPresent()) {
            if (availableLoc.get().getOfferedRoutes().size() == 1) {
                availableLoc.get().remove();
                NPCs.remove(availableLoc.get());
            }
        }
    }

    @Override
    protected void loadedEntity(TransportationPath entity) {
        paths.put(entity.getName().toLowerCase(), entity);

        // Find nearest NPC
        var startLoc = entity.getStartPoint().getLocation();
        // TODO: improve with map
        var availableLoc = NPCs.stream().filter(npc -> npc.getLocation().distanceSquared(startLoc) <= 16).findFirst();

        if (availableLoc.isPresent()) {
            availableLoc.get().addRoute(entity);
        } else {
            var npc = new TransportationNPC(startLoc);
            npc.addRoute(entity);
            npc.spawn();
            NPCs.add(npc);
        }
    }

    public TransportationNPC getNpc(Location location) {
        return NPCs.stream().filter(npc -> npc.getLocation().distanceSquared(location) <= 1).findFirst().orElse(null);
    }

    public TransportationPath getPath(String arg) {
        return paths.get(arg.toLowerCase());
    }

    public Map<String, TransportationPath> getPaths() {
        return Collections.unmodifiableMap(paths);
    }
}
