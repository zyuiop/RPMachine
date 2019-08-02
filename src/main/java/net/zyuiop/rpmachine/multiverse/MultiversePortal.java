package net.zyuiop.rpmachine.multiverse;

import net.zyuiop.rpmachine.common.Area;
import net.zyuiop.rpmachine.database.StoredEntity;

/**
 * @author Louis Vialar
 */
public class MultiversePortal implements StoredEntity {
    private Area portalArea;
    private String targetWorld;
    private String fileName;

    public MultiversePortal() {
    }

    public MultiversePortal(Area portalArea, String targetWorld) {
        this.portalArea = portalArea;
        this.targetWorld = targetWorld;
    }

    public Area getPortalArea() {
        return portalArea;
    }

    public String getTargetWorld() {
        return targetWorld;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
