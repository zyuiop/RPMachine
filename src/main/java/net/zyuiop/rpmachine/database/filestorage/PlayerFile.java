package net.zyuiop.rpmachine.database.filestorage;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.VirtualLocation;
import net.zyuiop.rpmachine.database.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * @author zyuiop
 */
public class PlayerFile implements PlayerData {
	private final YamlConfiguration data;
	private final File file;
	private final UUID uuid;

	public PlayerFile(UUID uuid, YamlConfiguration data, File file) {
		this.data = data;
		this.file = file;
		this.uuid = uuid;
	}

	@Override
	public String getJob() {
		return data.getString("job", null);
	}

	@Override
	public void setJob(String job) {
		data.set("job", job);
		save();
	}

	private void save() {
		try {
			data.save(file);
		} catch (IOException e) {
			RPMachine.getInstance().getLogger().severe("Error while saving playerData in " + file.getAbsolutePath());
			e.printStackTrace();
		}
	}

	@Override
	public VirtualLocation getHome() {
		String home = data.getString("rp.home", null);
		if (home == null) {
			return null;
		}

		return new VirtualLocation(home);
	}

	@Override
	public void setHome(VirtualLocation home) {
		data.set("rp.home", home.toString());
		save();
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
	public boolean isNew() {
		return !data.getKeys(false).contains("rpmoney");
	}

	@Override
	public boolean togglePlotMessages() {
		boolean val = !data.getBoolean("seemessages", true);
		data.set("seemessages", val);
		return val;
	}

	@Override
	public UUID getUuid() {
		return uuid;
	}
}
