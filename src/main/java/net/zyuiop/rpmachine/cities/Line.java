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

    public void display(Player player) {
        Location loc = start.clone();
        Vector vec = new Vector(start.getX() - end.getX(), start.getY() - end.getY(), start.getZ() - end.getZ());
        vec = vec.normalize().multiply(0.2); // 5 points per block

        while (loc.getBlockX() != end.getBlockX() && loc.getBlockZ() != end.getBlockZ()) {
            double y = loc.getWorld().getHighestBlockYAt(loc);

            Location part = loc.clone();
            for (double dy = 0.5; dy < 7; dy += 0.1) {
                part.setY(y + dy);

                player.spawnParticle(Particle.BARRIER, loc, 1);
            }

            loc = loc.add(vec);
        }
    }
}
