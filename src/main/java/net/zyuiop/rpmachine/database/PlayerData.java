package net.zyuiop.rpmachine.database;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.Sets;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.common.VirtualLocation;
import net.zyuiop.rpmachine.entities.LegalEntity;
import net.zyuiop.rpmachine.permissions.DelegatedPermission;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * @author zyuiop
 */
public class PlayerData implements LegalEntity {
	private final YamlConfiguration data;
	private final File file;
	private final UUID uuid;

	public PlayerData(UUID uuid, YamlConfiguration data, File file) {
		this.data = data;
		this.file = file;
		this.uuid = uuid;
	}

	public String getJob() {
		return data.getString("job", null);
	}

	public void setJob(String job) {
		data.set("job", job);
		save();
	}

	public String getName() {
		return RPMachine.database().getUUIDTranslator().getName(getUuid());
	}

	@Override
	public boolean hasDelegatedPermission(Player player, DelegatedPermission permission) {
		return player.getUniqueId().equals(getUuid()); // Player has all permissions on properties he manages!
	}

	private void save() {
		try {
			data.save(file);
		} catch (IOException e) {
			RPMachine.getInstance().getLogger().severe("Error while saving playerData in " + file.getAbsolutePath());
			e.printStackTrace();
		}
	}

	public VirtualLocation getHome() {
		String home = data.getString("rp.home", null);
		if (home == null) {
			return null;
		}

		return new VirtualLocation(home);
	}

	public void setHome(VirtualLocation home) {
		data.set("rp.home", home.toString());
		save();
	}

	@Override
	public boolean canActAs(Player p) {
		return p.getUniqueId().equals(getUuid());
	}

	@Override
	public double getBalance() {
		return Math.round(data.getDouble("rpmoney", 0.0) * 100) / 100;
	}

	@Override
	public void setBalance(double amount) {
		data.set("rpmoney", amount);
		save();
	}

	@Override
	public boolean withdrawMoney(double amount) {
		if (data.getDouble("rpmoney", 0D) >= amount) {
			data.set("rpmoney", data.getDouble("rpmoney", 0D) - amount);
			save();
			return true;
		}
		return false;
	}

	@Override
	public void creditMoney(double amount) {
		data.set("rpmoney", data.getDouble("rpmoney", 0D) + amount);
		save();
	}

	@Override
	public void setUnpaidTaxes(String city, double amount) {
		if (amount <= 0) {
			data.set("unpaid." + city, null);
		} else {
			data.set("unpaid." + city, amount);
		}
		save();
	}

	@Override
	public double getUnpaidTaxes(String city) {
		return data.getDouble("topay." + city, 0);
	}

	@Override
	public void setLastTaxes(String city, Date nd) {
		GregorianCalendar date = new GregorianCalendar();
		date.setTime(nd);
		String dateString = date.get(Calendar.DAY_OF_MONTH) + "/" + date.get(Calendar.MONTH) + "/" + date.get(Calendar.YEAR);

		data.set("lasttaxes." + city, dateString);
		save();
	}

	@Override
	public Date getLastTaxes(String city) {
		String data = this.data.getString("lasttaxes." + city, "none");
		if (data.equals("none")) {
			return null;
		}

		String[] parts = data.split("/");
		GregorianCalendar calendar = new GregorianCalendar(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
		return calendar.getTime();
	}

	@Override
	public Map<String, Double> getUnpaidTaxes() {
		Set<String> dataKeys = data.getKeys(false);
		return dataKeys.stream()
				.filter(key -> key.startsWith("topay."))
				.collect(Collectors.toMap(key -> key.split("\\.")[1], data::getDouble));
	}

	@Override
	public String displayable() {
		return ChatColor.YELLOW + "(Joueur) " + ChatColor.GOLD + getName();
	}

	@Override
	public String shortDisplayable() {
		return ChatColor.GOLD + getName();
	}

	@Override
	public Set<UUID> getAdministrators() {
		return Sets.newHashSet(uuid);
	}

	public boolean isNew() {
		return !data.getKeys(false).contains("rpmoney");
	}

	public boolean togglePlotMessages() {
		boolean val = !data.getBoolean("seemessages", true);
		data.set("seemessages", val);
		return val;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setAttribute(String key, Object value) {
		data.set("attr." + key, value);
		save();
	}

	public boolean hasAttribute(String key) {
		return data.contains("attr." + key);
	}

	public <T> T getAttribute(String key) {
		return (T) data.get("attr." + key);
	}

	public Map<Material, Integer> getCollectedItems() {
		ConfigurationSection section = data.getConfigurationSection("collected_items");

		if (section == null || section.getKeys(false).isEmpty())
			return Collections.emptyMap();

		Map<Material, Integer> map = new HashMap<>();
		section.getKeys(false).forEach(k -> {
			try {
				map.put(Material.valueOf(k), section.getInt(k));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return map;
	}

	public int getCollectedItems(Material material) {
		return data.getInt("collected_items." + material.name(), 0);
	}

	public void addCollectedItems(Material material, int amt) {
		data.set("collected_items." + material.name(), getCollectedItems(material) + amt);
		save();
	}

	public void resetCollectedItems() {
		data.set("collected_items", null);
	}
}
