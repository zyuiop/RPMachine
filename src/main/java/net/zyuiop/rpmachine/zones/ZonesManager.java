package net.zyuiop.rpmachine.zones;

import com.google.gson.Gson;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.CitiesComparator;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.cities.data.CityFloor;
import net.zyuiop.rpmachine.common.VirtualChunk;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_10_R1.util.LongObjectHashMap;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author zyuiop
 */
public class ZonesManager {
	private final File zonesFolder;
	private final RPMachine rpMachine;
	private ConcurrentHashMap<String, Zone> zones = new ConcurrentHashMap<>();

	public ZonesManager(RPMachine plugin) {
		this.rpMachine = plugin;
		zonesFolder = new File(plugin.getDataFolder().getPath() + "/zones");
		if (!zonesFolder.isDirectory())
			return;

		Gson gson = new Gson();

		for (File file : zonesFolder.listFiles()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				Zone zone = gson.fromJson(reader, Zone.class);
				zones.put(zone.getPlotName(), zone);
				plugin.getLogger().info("Loaded zone " + zone.getPlotName());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public Zone getZone(String name) {
		return zones.get(name);
	}

	public ConcurrentHashMap<String, Zone> getZones() {
		return zones;
	}

	public boolean createZone(Zone zone) {
		if (zones.containsKey(zone.getPlotName()))
			return false;

		String fileName = zone.getPlotName().replace("/", "_");
		fileName = fileName.replace("\\", "_");
		File file = new File(zonesFolder, fileName + ".json");
		if (file.exists()) {
			int i = 1;
			while (i <= 100 && file.exists()) {
				file = new File(zonesFolder, fileName + "(" + i + ").json");
				i++;
			}

			if (file.exists())
				return false;
		}

		try {
			zone.setFileName(file.getName());
			this.zones.put(zone.getPlotName(), zone);
			boolean create = file.createNewFile();
			if (!create)
				return false;
			saveZone(zone);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void saveZone(Zone zone) {
		new Thread(() -> {
			File file = new File(zonesFolder, zone.getFileName());
			if (!file.exists())
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}

			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(file));
				new Gson().toJson(zone, Zone.class, writer);
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}).start();
	}

	public Zone getZoneHere(Location location) {
		for (Zone zone : zones.values()) {
			if (zone.getArea().isInside(location))
				return zone;
		}
		return null;
	}

	public boolean canBuild(Player player, Location location) {
		Zone zone = getZoneHere(location);
		return zone == null || zone.canBuild(player, location);
	}

	public boolean canInteractWithBlock(Player player, Location location) {
		return canBuild(player, location);
	}

	public void removeZone(Zone zone) {
		File file = new File(zonesFolder, zone.getFileName());
		file.delete();
		zones.remove(zone.getPlotName());
	}

	public Collection<Zone> getZonesHere(Chunk chunk) {
		return zones.values().stream().filter(zone -> zone.getArea().hasCommonPositionsWith(chunk)).collect(Collectors.toList());
	}
}
