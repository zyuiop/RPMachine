package net.zyuiop.rpmachine.multiverse;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.filestorage.FileEntityStore;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Louis Vialar
 */
public class MultiverseManager extends FileEntityStore<MultiverseWorld> {
    private Map<String, MultiverseWorld> worlds = new HashMap<>();
    private PortalsManager manager;

    public MultiverseManager() {
        super(MultiverseWorld.class, "multiverses");

        load();

        manager = new PortalsManager();

        if (getWorld("world") == null) {
            createWorld(new MultiverseWorld("world", false, true, true));
        }

        Bukkit.getServer().getPluginManager().registerEvents(new MultiverseListener(this), RPMachine.getInstance());
        new CreatePortalCommand(this); // Init portal creation
        new ListMultiversesCommand(this); // Init portal list
    }

    public Collection<MultiverseWorld> getWorlds() {
        return worlds.values();
    }

    public MultiverseWorld getWorld(String world) {
        return worlds.get(world);
    }

    public void createWorld(MultiverseWorld worldToCreate) {
        createEntity(worldToCreate.getWorldName(), worldToCreate);

        // Start generation
        if (!worldToCreate.getWorldName().equalsIgnoreCase("world"))
            Bukkit.createWorld(new WorldCreator(worldToCreate.getWorldName()));
    }

    public void deleteWorld(MultiverseWorld mvWorld) {
        if (mvWorld.getWorldName().equalsIgnoreCase("world"))
            return;

        Bukkit.getLogger().info("Deleting multiverse " + mvWorld.getWorldName());

        removeEntity(mvWorld);

        // Remove portals
        for (MultiversePortal portal : mvWorld.getPortals())
            manager.deletePortal(portal);

        // Relocate players
        World world = mvWorld.getWorld();
        world.getPlayers().forEach(p -> {
            Bukkit.getLogger().info("Player " + p.getName() + " is in the deleted world, relocating...");
            Location target = p.getLocation().clone();
            target.setWorld(Bukkit.getWorld("world"));
            target.setY(Bukkit.getWorld("world").getHighestBlockYAt(target));
            p.teleport(target);
        });

        // Unload and remove
        Bukkit.unloadWorld(world, false);

        File worldDir = world.getWorldFolder();
        try {
            Bukkit.getLogger().info("Deleting world folder " + worldDir.getAbsolutePath());
            FileUtils.deleteDirectory(worldDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createPortal(MultiversePortal portal) {
        manager.createPortal(portal);
    }

    public void deletePortal(MultiversePortal portal) {
        MultiverseWorld world = getWorld(portal.getPortalArea().getWorld());
        if (world != null) {
            world.deletePortal(portal);
            saveEntity(world);
        }

        manager.deletePortal(portal);
    }

    @Override
    protected void loadedEntity(MultiverseWorld entity) {
        worlds.put(entity.getWorldName(), entity);
    }

    class PortalsManager extends FileEntityStore<MultiversePortal> {
        public PortalsManager() {
            super(MultiversePortal.class, "portals");

            load();
        }

        @Override
        protected void loadedEntity(MultiversePortal entity) {
            String worldName = entity.getPortalArea().getWorld();
            MultiverseWorld world = getWorld(worldName);

            world.addPortal(entity);
        }

        public void deletePortal(MultiversePortal p) {
            removeEntity(p);
        }

        public void createPortal(MultiversePortal portal) {
            createEntity(portal.getPortalArea().toString(), portal);
        }
    }
}
