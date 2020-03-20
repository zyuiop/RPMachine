package net.zyuiop.rpmachine.cities;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * @author Louis Vialar
 */
public class Line {
    private Location start;
    private Location end;

    public Line(Location start, Location end) {
        this.start = start;
        this.end = end;
    }

    public void displayWallFullHeight(Player player) {
        displayWallFullHeight(player, Particle.BARRIER, 1.5, 1);
    }

    public void displayWallFullHeight(Player player, Particle particle, double verticalSpacing, double horizontalSpacing) {
        Location loc = start.clone();
        Vector vec = end.toVector().subtract(start.toVector());
        vec = vec.normalize().multiply(horizontalSpacing); // 1 point per block

        loc = loc.subtract(vec);
        do {
            loc = loc.add(vec);
            double y = loc.getWorld().getHighestBlockYAt(loc);

            Location part = loc.clone();
            for (double i = 0.5; i < y + 7; i += verticalSpacing) {
                if (!loc.getWorld().getBlockAt(loc.getBlockX(), (int) i, loc.getBlockZ()).isEmpty())
                    continue;

                part.setY(i);
                player.spawnParticle(particle, part, 1);
            }
        } while (loc.getBlockX() != end.getBlockX() || loc.getBlockZ() != end.getBlockZ());

    }

    public void displayWall(Player player) {
        displayWall(player, Particle.BARRIER, 1.5, 1);
    }

    public void displayWall(Player player, Particle particle, double verticalSpacing, double horizontalSpacing) {
        Location loc = start.clone();
        Vector vec = end.toVector().subtract(start.toVector()).setY(0);
        vec = vec.normalize().multiply(horizontalSpacing); // 1 point per block

        loc = loc.subtract(vec);
        do {
            loc = loc.add(vec);

            Location part = loc.clone();
            for (double i = start.getY() ; i < end.getY() ; i += verticalSpacing) {
                if (!loc.getWorld().getBlockAt(loc.getBlockX(), (int) i, loc.getBlockZ()).isEmpty())
                    continue;

                part.setY(i);
                player.spawnParticle(particle, part, 1);
            }
        } while (loc.getBlockX() != end.getBlockX() || loc.getBlockZ() != end.getBlockZ());

    }

    public void displayLine(Player player) {
        displayLine(player, Particle.DRIP_LAVA, 0.2);
    }

    public void displayLine(Player player, Particle particle, double spacing) {
        Location loc = start.clone();
        Vector vec = end.toVector().subtract(start.toVector());

        var distance = vec.length();
        vec = vec.normalize().multiply(spacing); // 1 point per block

        for (double i = 0.0 ; i < distance ; i += spacing) {
            loc = loc.add(vec);
            player.spawnParticle(particle, loc, 1, 0, 0, 0);
        }
    }

    @Override
    public String toString() {
        return "Line " + start + " to " + end;
    }
}
