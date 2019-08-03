package net.zyuiop.rpmachine.multiverse;

import net.zyuiop.rpmachine.RPMachine;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author Louis Vialar
 */
public class MultiverseWorld {

    static enum RegenFrequency {
        DAILY, WEEKLY, MONTHLY, EVERY_REBOOT, NEVER
    }

    private final String worldName;
    private final boolean allowGeneration;
    private final boolean allowNether;
    private final boolean allowEnd;
    private final MultiverseManager.RegenFrequency frequency;
    private final List<MultiversePortal> portals = new ArrayList<>();
    private boolean regenStarted = false;
    private boolean forcedRegen = false;

    public MultiverseWorld(String worldName, boolean allowGeneration, boolean allowNether, boolean allowEnd, MultiverseManager.RegenFrequency frequency) {
        this.worldName = worldName;
        this.allowGeneration = allowGeneration;
        this.allowNether = allowNether;
        this.allowEnd = allowEnd;
        this.frequency = frequency;
    }

    public String getWorldName() {
        return worldName;
    }

    public List<MultiversePortal> getPortals() {
        return portals;
    }

    public MultiversePortal getPortal(Location location) {
        for (MultiversePortal p : portals)
            if (p.getPortalArea().isInside(location))
                return p;
        return null;
    }

    public boolean isAllowGeneration() {
        return allowGeneration;
    }

    public boolean isAllowNether() {
        return allowNether;
    }

    public boolean isAllowEnd() {
        return allowEnd;
    }

    void addPortal(MultiversePortal nPortal) {
        portals.add(nPortal);
    }

    void deletePortal(MultiversePortal nPortal) {
        portals.remove(nPortal);
    }

    void generateWorld() {
        if (!worldName.equalsIgnoreCase("world")) {
            Bukkit.getLogger().info("Loading multiverse " + worldName);
            Bukkit.createWorld(new WorldCreator(worldName));

            if (isAllowNether()) {
                Bukkit.createWorld(new WorldCreator(worldName + "_nether").environment(World.Environment.NETHER));
            }

            if (isAllowEnd()) {
                Bukkit.createWorld(new WorldCreator(worldName + "_the_end").environment(World.Environment.THE_END));
            }
        }
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    private void removeWorld(String name) {
        // Unload and remove
        World world = Bukkit.getWorld(name);

        Bukkit.getLogger().info(" -- Deleting world " + name);

        // Relocate players
        world.getPlayers().forEach(p -> {
            Bukkit.getLogger().info(" -- .. Player " + p.getName() + " is in the deleted world, relocating...");
            Location target = p.getLocation().clone();
            target.setWorld(Bukkit.getWorld("world"));
            target.setY(Bukkit.getWorld("world").getHighestBlockYAt(target));
            p.teleport(target);
        });

        Bukkit.unloadWorld(world, false);

        File worldDir = world.getWorldFolder();
        try {
            Bukkit.getLogger().info(" -- .. Deleting world folder " + worldDir.getAbsolutePath());
            FileUtils.deleteDirectory(worldDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void removeWorld() {
        // Unload and remove
        removeWorld(worldName);

        if (isAllowNether()) {
            removeWorld(worldName + "_nether");
        }

        if (isAllowEnd()) {
            removeWorld(worldName + "_the_end");
        }
    }

    void setForcedRegen() {
        this.forcedRegen = true;
    }

    private boolean checkNeedsRegen(boolean autoReboot) {
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
        RPMachine.getInstance().getLogger().info("World " + getWorldName() + " has regen frequency " + frequency + " and needs regen, deleting it.");
        RPMachine.getInstance().getMultiverseManager().deleteWorld(this);
    }
}
