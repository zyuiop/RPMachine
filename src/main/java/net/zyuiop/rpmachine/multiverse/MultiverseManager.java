package net.zyuiop.rpmachine.multiverse;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.filestorage.FileEntityStore;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Louis Vialar
 */
public class MultiverseManager extends FileEntityStore<MultiverseWorld> {
    private Map<String, MultiverseWorld> worlds = new HashMap<>();
    private PortalsManager manager;
    private List<ConfiguredMultiverse> configs = new ArrayList<>();

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
        new ForceRegenCommand(this); // Init portal list

        // Load config
        Configuration config = RPMachine.getInstance().getConfig();
        if (config.contains("multiverses")) {
            for (Map<?, ?> map : config.getMapList("multiverses")) {
                String name = (String) map.get("name");
                boolean genPlatform = (Boolean) map.get("generatePlatform");
                boolean allowNether = (Boolean) map.get("allowNether");
                boolean allowEnd = (Boolean) map.get("allowEnd");
                RegenFrequency regenFrequency = RegenFrequency.valueOf((String) map.get("regenFrequency"));

                configs.add(new ConfiguredMultiverse(new MultiverseWorld(name, genPlatform, allowNether, allowEnd), regenFrequency));
                RPMachine.getInstance().getLogger().info("Loaded multiverse config " + name + " " + regenFrequency);
            }
        }
    }

    public void generateWorlds() {
        configs.forEach(ConfiguredMultiverse::checkWorldExists);
    }

    public void onShutdown(boolean automatic) {
        configs.forEach(c -> c.regenIfNeeded(automatic));
    }

    public int forceRegen(String world) {
        return configs.stream().filter(c -> c.world.getWorldName().equalsIgnoreCase(world)).mapToInt(c -> {
            c.setForcedRegen(true);
            return 1;
        }).sum();
    }

    public Collection<MultiverseWorld> getWorlds() {
        return worlds.values();
    }

    public MultiverseWorld getWorld(String world) {
        return worlds.get(world);
    }

    public void createWorld(MultiverseWorld worldToCreate) {
        createEntity(worldToCreate.getWorldName(), worldToCreate);

        generateWorld(worldToCreate);
    }

    public void generateWorld(MultiverseWorld worldToGenerate) {
        // Start generation
        if (!worldToGenerate.getWorldName().equalsIgnoreCase("world"))
            Bukkit.createWorld(new WorldCreator(worldToGenerate.getWorldName()));
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

    static enum RegenFrequency {
        DAILY, WEEKLY, MONTHLY, EVERY_REBOOT, NEVER
    }

    class ConfiguredMultiverse {
        private final MultiverseWorld world;
        private final RegenFrequency frequency;
        private boolean regenStarted = false;
        private boolean forcedRegen = false;

        ConfiguredMultiverse(MultiverseWorld world, RegenFrequency frequency) {
            this.world = world;
            this.frequency = frequency;
        }

        void checkWorldExists() {
            if (getWorld(world.getWorldName()) == null) {
                RPMachine.getInstance().getLogger().info("World " + world.getWorldName() + " does not exist, creating it.");
                createWorld(new MultiverseWorld(world.getWorldName(), world.isAllowGeneration(), world.isAllowNether(), world.isAllowEnd()));
            } else {
                RPMachine.getInstance().getLogger().info("World " + world.getWorldName() + " already exists, no need to generate.");
                generateWorld(getWorld(world.getWorldName()));
            }
        }

        void setForcedRegen(boolean forcedRegen) {
            this.forcedRegen = forcedRegen;
        }

        boolean checkNeedsRegen(boolean autoReboot) {
            if (forcedRegen)
                return true;

            Calendar cal = new GregorianCalendar();

            switch (frequency) {
                case EVERY_REBOOT:
                    return true;
                case MONTHLY:
                    return autoReboot && cal.get(Calendar.DAY_OF_MONTH) == 1;
                case DAILY:
                    return autoReboot;
                case WEEKLY:
                    return autoReboot && cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY;
                case NEVER:
                default:
                    return false;
            }
        }

        void regenIfNeeded(boolean autoReboot) {
            if (regenStarted || !checkNeedsRegen(autoReboot))
                return;

            regenStarted = true;
            RPMachine.getInstance().getLogger().info("World " + world.getWorldName() + " has regen frequency " + frequency + " and needs regen, deleting it.");
            deleteWorld(getWorld(world.getWorldName()));
        }
    }
}
