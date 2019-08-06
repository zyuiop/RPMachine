package net.zyuiop.rpmachine.multiverse;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.filestorage.FileEntityStore;
import org.bukkit.Bukkit;
import org.bukkit.configuration.Configuration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Louis Vialar
 */
public class MultiverseManager {
    private Map<String, MultiverseWorld> worlds = new HashMap<>();
    private PortalsManager manager;

    public MultiverseManager() {
        // Load config
        Configuration config = RPMachine.getInstance().getConfig();
        if (config.contains("multiverses")) {
            for (Map<?, ?> map : config.getMapList("multiverses")) {
                String name = (String) map.get("name");
                boolean genPlatform = (Boolean) map.get("generatePlatform");
                boolean allowNether = (Boolean) map.get("allowNether");
                boolean allowEnd = (Boolean) map.get("allowEnd");
                RegenFrequency regenFrequency = RegenFrequency.valueOf((String) map.get("regenFrequency"));

                MultiverseWorld w = new MultiverseWorld(name, genPlatform, allowNether, allowEnd, regenFrequency);
                if (map.containsKey("forceLoad"))
                    w.setForceLoad((Boolean) map.get("forceLoad"));

                if (map.containsKey("forceLoadArea"))
                    w.setLoadSize((Integer) map.get("forceLoadArea"));

                if (map.containsKey("loadSpeed"))
                    w.setLoadSpeed((Integer) map.get("loadSpeed"));

                worlds.put(name, w);
                RPMachine.getInstance().getLogger().info("Loaded multiverse config " + name + " " + regenFrequency);
            }
        }

        if (getWorld("world") == null) {
            worlds.put("world", new MultiverseWorld("world", false, true, true, RegenFrequency.NEVER));
        }

        manager = new PortalsManager();

        Bukkit.getServer().getPluginManager().registerEvents(new MultiverseListener(this), RPMachine.getInstance());
        new CreatePortalCommand(this); // Init portal creation
        new ListMultiversesCommand(this); // Init portal list
        new ForceRegenCommand(this); // Init portal list
    }

    public void generateWorlds() {
        worlds.values().forEach(MultiverseWorld::generateWorld);
    }

    public void onShutdown(boolean automatic) {
        worlds.values().forEach(c -> c.regenIfNeeded(automatic));
    }

    public int forceRegen(String world) {
        MultiverseWorld w = worlds.get(world);

        if (w != null) {
            w.setForcedRegen();
            return 1;
        }
        return 0;
    }

    public Collection<MultiverseWorld> getWorlds() {
        return worlds.values();
    }

    public MultiverseWorld getWorld(String world) {
        return worlds.get(world);
    }

    public void deleteWorld(MultiverseWorld mvWorld) {
        if (mvWorld.getWorldName().equalsIgnoreCase("world"))
            return;

        Bukkit.getLogger().info("Deleting multiverse " + mvWorld.getWorldName());

        // Remove portals
        for (MultiversePortal portal : mvWorld.getPortals())
            manager.deletePortal(portal);

        // Delete the actual world
        mvWorld.removeWorld();
    }

    public void createPortal(MultiversePortal portal) {
        manager.createPortal(portal);
    }

    public void deletePortal(MultiversePortal portal) {
        MultiverseWorld world = getWorld(portal.getPortalArea().getWorld());
        if (world != null) {
            world.deletePortal(portal);
        }

        manager.deletePortal(portal);
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

    static enum RegenFrequency {
        DAILY, WEEKLY, MONTHLY, EVERY_REBOOT, NEVER
    }
}
