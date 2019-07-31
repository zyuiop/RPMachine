package net.zyuiop.rpmachine.projects;

import com.google.gson.Gson;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.json.Json;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author zyuiop
 */
public class ProjectsManager {
	private final File zonesFolder;
	private final RPMachine rpMachine;
	private ConcurrentHashMap<String, Project> zones = new ConcurrentHashMap<>();

	public ProjectsManager(RPMachine plugin) {
		this.rpMachine = plugin;
		zonesFolder = new File(plugin.getDataFolder().getPath() + "/projects");
		if (!zonesFolder.isDirectory())
			return;

		for (File file : zonesFolder.listFiles()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				Project project = Json.GSON.fromJson(reader, Project.class);
				zones.put(project.getPlotName(), project);
				plugin.getLogger().info("Loaded project " + project.getPlotName());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public Project getZone(String name) {
		return zones.get(name);
	}

	public ConcurrentHashMap<String, Project> getZones() {
		return zones;
	}

	public boolean createZone(Project project) {
		if (zones.containsKey(project.getPlotName()))
			return false;

		String fileName = project.getPlotName().replace("/", "_");
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
			project.setFileName(file.getName());
			this.zones.put(project.getPlotName(), project);
			boolean create = file.createNewFile();
			if (!create)
				return false;
			saveZone(project);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void saveZone(Project project) {
		new Thread(() -> {
			File file = new File(zonesFolder, project.getFileName());
			if (!file.exists())
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}

			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(file));
				new Gson().toJson(project, Project.class, writer);
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}).start();
	}

	public Project getZoneHere(Location location) {
		for (Project project : zones.values()) {
			if (project.getArea().isInside(location))
				return project;
		}
		return null;
	}

	public boolean canBuild(Player player, Location location) {
		Project project = getZoneHere(location);
		return project == null || project.canBuild(player, location);
	}

	public boolean canInteractWithBlock(Player player, Location location) {
		return canBuild(player, location);
	}

	public void removeZone(Project project) {
		File file = new File(zonesFolder, project.getFileName());
		file.delete();
		zones.remove(project.getPlotName());
	}

	public Collection<Project> getZonesHere(Chunk chunk) {
		return zones.values().stream().filter(zone -> zone.getArea().hasCommonPositionsWith(chunk)).collect(Collectors.toList());
	}
}
