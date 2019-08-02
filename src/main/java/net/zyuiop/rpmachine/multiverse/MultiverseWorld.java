package net.zyuiop.rpmachine.multiverse;

import net.zyuiop.rpmachine.database.StoredEntity;
import net.zyuiop.rpmachine.json.JsonExclude;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Louis Vialar
 */
public class MultiverseWorld implements StoredEntity {
    private String worldName;
    @JsonExclude
    private List<MultiversePortal> portals = new ArrayList<>();
    private boolean allowGeneration;
    private boolean allowNether;
    private boolean allowEnd;
    private String fileName;

    public MultiverseWorld() {
    }

    public MultiverseWorld(String worldName, boolean generatePlatform, boolean allowNether, boolean allowEnd) {
        this.worldName = worldName;
        this.allowGeneration = generatePlatform;
        this.allowNether = allowNether;
        this.allowEnd = allowEnd;
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

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    void addPortal(MultiversePortal nPortal) {
        portals.add(nPortal);
    }

    void deletePortal(MultiversePortal nPortal) {
        portals.remove(nPortal);
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public void setFileName(String name) {
        this.fileName = name;
    }
}
