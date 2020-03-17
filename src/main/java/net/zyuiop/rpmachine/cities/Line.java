package net.zyuiop.rpmachine.cities;

import org.bukkit.ChatColor;
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
        Vector vec = new Vector(start.getX() - end.getX(), start.getY() - end.getY(), start.getZ() - end.getZ()).multiply(-1);
        vec = vec.normalize(); // 1 point per block
        while (loc.getBlockX() != end.getBlockX() || loc.getBlockZ() != end.getBlockZ()) {
            double y = loc.getWorld().getHighestBlockYAt(loc);

            Location part = loc.clone();
            for (double i = 0.5; i < y + 7; i += 1.5) {
                if (!loc.getWorld().getBlockAt(loc.getBlockX(), (int) i, loc.getBlockZ()).isEmpty())
                    continue;

                part.setY(i);
                player.spawnParticle(Particle.BARRIER, part, 1);
            }

            loc = loc.add(vec);
        }
    }

    @Override
    public String toString() {
        return "Line " + start + " to " + end;
    }
}
