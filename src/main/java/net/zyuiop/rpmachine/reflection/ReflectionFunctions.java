package net.zyuiop.rpmachine.reflection;

import net.zyuiop.rpmachine.scoreboards.ScoreboardSign;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author zyuiop
 */
public interface ReflectionFunctions {

	void launchfw(final Location loc, final FireworkEffect effect);

	void playEndermanTeleport(Location location, Player player);

	ScoreboardSign createScoreboardSign(Player player, String text);
}
