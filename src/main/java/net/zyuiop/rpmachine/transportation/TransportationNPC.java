package net.zyuiop.rpmachine.transportation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TransportationNPC {
    // TODO: abstract NPC logic away for future uses
    private Location location;
    private List<TransportationPath> offeredRoutes = new ArrayList<>();
    private Villager villager;

    public TransportationNPC(Location location) {
        this.location = location;
    }

    public void addRoute(TransportationPath path) {
        this.offeredRoutes.add(path);
    }

    public void spawn() {
        Bukkit.getLogger().info("[Transportation] Spawning NPC at " + location);
        var nearbyVillagers = Arrays.stream(location.getChunk().getEntities())
                .filter(e -> e.getLocation().distanceSquared(location) <= 1 && e.getType() == EntityType.VILLAGER && e.isInvulnerable())
                .collect(Collectors.toList());

        if (nearbyVillagers.size() > 0) {
            Bukkit.getLogger().info("[Transportation] .. Clearing " + nearbyVillagers.size() + " old NPCs at " + location);
            nearbyVillagers.forEach(Entity::remove);
        }

        this.villager = location.getWorld().spawn(location, Villager.class, v -> {
            v.setProfession(Villager.Profession.NITWIT);
            v.setAware(false);
            v.setAI(false);
            v.setInvulnerable(true);
            v.setCustomNameVisible(true);
            v.setCustomName(ChatColor.DARK_AQUA + "Hystori'air");
        });
    }

    public void remove() {
        this.villager.remove();
    }

    public void onClick(Player player) {
        new TransportationSelectGUI(player, offeredRoutes).open();
    }

    public Location getLocation() {
        return location;
    }

    public List<TransportationPath> getOfferedRoutes() {
        return offeredRoutes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransportationNPC that = (TransportationNPC) o;
        return Objects.equals(location.getBlock(), that.location.getBlock());
    }

    @Override
    public int hashCode() {
        return Objects.hash(location.getBlock());
    }
}
