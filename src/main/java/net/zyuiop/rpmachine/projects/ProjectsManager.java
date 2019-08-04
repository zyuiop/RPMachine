package net.zyuiop.rpmachine.projects;

import com.google.gson.Gson;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.database.filestorage.FileEntityStore;
import net.zyuiop.rpmachine.entities.LegalEntityRepository;
import net.zyuiop.rpmachine.json.Json;
import org.bukkit.Bukkit;
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
public class ProjectsManager extends FileEntityStore<Project> implements LegalEntityRepository<Project> {
	private final RPMachine rpMachine;
	private ConcurrentHashMap<String, Project> zones = new ConcurrentHashMap<>();

	public ProjectsManager(RPMachine plugin) {
		super(Project.class, "projects");

		this.rpMachine = plugin;

		super.load();
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

		return super.createEntity(fileName, project);
	}

	public void saveZone(Project project) {
		super.saveEntity(project);
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
		super.removeEntity(project);
		zones.remove(project.getPlotName());
	}

	public Collection<Project> getZonesHere(Chunk chunk) {
		return zones.values().stream().filter(zone -> zone.getArea().hasCommonPositionsWith(chunk)).collect(Collectors.toList());
	}

	@Override
	public Project findEntity(String tag) {
		return getZone(tag);
	}

	@Override
	public String getTag(Project entity) {
		return entity.getPlotName();
	}

	@Override
	protected void loadedEntity(Project project) {
		zones.put(project.getPlotName(), project);
	}
}
