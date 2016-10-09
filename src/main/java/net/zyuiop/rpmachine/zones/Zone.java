package net.zyuiop.rpmachine.zones;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.common.Area;
import net.zyuiop.rpmachine.common.Plot;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author zyuiop
 *         A zone is a kind of plot located outside of any city. It can only be created with administrative permissions.<br/>
 *         A zone has no taxes as it doesn't depend of any city.
 */
public class Zone extends Plot {
	private String welcomeMessage;
	private String goodByeMessage;
	private String fileName;

	public String getWelcomeMessage() {
		return welcomeMessage;
	}

	public void setWelcomeMessage(String welcomeMessage) {
		this.welcomeMessage = welcomeMessage;
	}

	public String getGoodByeMessage() {
		return goodByeMessage;
	}

	public void setGoodByeMessage(String goodByeMessage) {
		this.goodByeMessage = goodByeMessage;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public boolean checkArea(Area area, ZonesManager manager, Player player) {
		int i_x = area.getMinX();
		while (i_x < area.getMaxX()) {
			int i_z = area.getMinZ();
			while (i_z < area.getMaxZ()) {
				if (RPMachine.getInstance().getCitiesManager().getCityHere(new Location(Bukkit.getWorld("world"), i_x, 64, i_z).getChunk()) != null) {
					player.sendMessage(ChatColor.RED + "Une partie de votre sélection est dans une ville.");
					return false;
				}

				int i_y = area.getMinY();
				while (i_y < area.getMaxY()) {
					Zone check = manager.getZoneHere(new Location(Bukkit.getWorld("world"), i_x, i_y, i_z));
					if (check != null && !check.getPlotName().equals(getPlotName())) {
						player.sendMessage(ChatColor.RED + "Une partie de votre sélection fait partie d'une autre zone.");
						return false;
					}
					i_y ++;
				}
				i_z ++;
			}
			i_x ++;
		}

		setArea(area);
		return true;
	}
}
