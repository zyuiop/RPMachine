package net.zyuiop.rpmachine.reflection;

import net.minecraft.server.v1_10_R1.EntityFireworks;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.ScoreboardSign;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftFirework;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 * @author zyuiop
 */
public class v1_10_R1 implements ReflectionFunctions {
	@Override
	public void launchfw(Location loc, FireworkEffect effect) {
		final Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();
		fwm.addEffect(effect);
		fwm.setPower(0);
		fw.setFireworkMeta(fwm);
		((CraftFirework) fw).getHandle().setInvisible(true);
		Bukkit.getScheduler().runTaskLater(RPMachine.getInstance(), () -> {
			net.minecraft.server.v1_10_R1.World w = (((CraftWorld) loc.getWorld()).getHandle());
			EntityFireworks fireworks = ((CraftFirework) fw).getHandle();
			w.broadcastEntityEffect(fireworks, (byte) 17);
			fireworks.die();
		}, 5);
	}

	@Override
	public void playEndermanTeleport(Location location, Player player) {
		player.playSound(location, Sound.valueOf("ENTITY_ENDERMEN_TELEPORT"), 1, 1);
	}

	@Override
	public ScoreboardSign createScoreboardSign(Player player, String text) {
		return new ScoreboardSign_v1_10_R1(player, text);
	}
}
