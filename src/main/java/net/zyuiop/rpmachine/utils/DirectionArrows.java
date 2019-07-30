package net.zyuiop.rpmachine.utils;

import org.bukkit.Location;

/**
 * @author Louis Vialar
 */
public class DirectionArrows {
    public static String getArrow(Location from, Location to) {
        float yaw = from.getYaw() > 0F ? from.getYaw() : from.getYaw() + 360F;

        int x1 = from.getBlockX();
        int z1 = from.getBlockZ();

        int x2 = to.getBlockX();
        int z2 = to.getBlockZ();

        float angle = (float) Math.atan2(x1 - x2, z1 - z2);

        angle += Math.PI / 2;

        angle = (float) Math.toDegrees(angle);

        if (angle < 0) {
            angle += 360;
        }

        // Vers où se situe le joueur en cardinalité de son allié
        int coord = 0;
        if (angle < 292.5F && angle >= 247.5F)
            coord = 0; // Nord
        else if (angle < 247.5F && angle >= 202.5F)
            coord = 1; // Nord Est
        else if (angle < 202.5F && angle >= 157.5F)
            coord = 2; // Est
        else if (angle < 157.5F && angle >= 112.5F)
            coord = 3; // Sud Est
        else if (angle < 112.5F && angle >= 67.5F)
            coord = 4; // Sud
        else if (angle < 67.5F && angle >= 22.5F)
            coord = 5; // Sud Ouest
        else if (angle < 22.5F || angle >= 337.5F)
            coord = 6; // Ouest
        else if (angle < 337.5F && angle >= 292.5F)
            coord = 7; // Nord Ouest

        String result = "§7?";
        // La flèche en fonction de la zone

        yaw -= 45 * coord;
        if (yaw < 0)
            yaw += 360;

        if (yaw <= 22.5 || yaw > 337.5)
            result = SymbolBank.ARROW_UP;
        else if (yaw <= 337.5 && yaw > 292.5)
            result = SymbolBank.ARROW_UP_RIGHT;
        else if (yaw <= 292.5 && yaw > 247.5)
            result = SymbolBank.ARROW_RIGHT;
        else if (yaw <= 247.5 && yaw > 202.5)
            result = SymbolBank.ARROW_DOWN_RIGHT;
        else if (yaw <= 202.5 && yaw > 157.5)
            result = SymbolBank.ARROW_DOWN;
        else if (yaw <= 157.5 && yaw > 112.5)
            result = SymbolBank.ARROW_DOWN_LEFT;
        else if (yaw <= 112.5 && yaw > 67.5)
            result = SymbolBank.ARROW_LEFT;
        else if (yaw <= 67.5 && yaw > 22.5)
            result = SymbolBank.ARROW_UP_LEFT;
        return result;
    }
}
