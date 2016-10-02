package net.zyuiop.rpmachine.database.bukkitbridge;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.bridgesapi.api.player.PlayerData;
import net.zyuiop.rpmachine.VirtualLocation;

/**
 * @author zyuiop
 */
public class BukkitBridgePlayerData implements net.zyuiop.rpmachine.database.PlayerData {
	private final PlayerData data;

	public BukkitBridgePlayerData(PlayerData data) {
		this.data = data;
	}

	@Override
	public String getJob() {
		return data.get("job", null);
	}

	@Override
	public void setJob(String job) {
		if (job == null)
			data.remove("job");
		else
			data.set("job", job);
	}

	@Override
	public VirtualLocation getHome() {
		String home = data.get("rp.home", null);
		if (home == null) {
			return null;
		}

		return new VirtualLocation(home);
	}

	@Override
	public void setHome(VirtualLocation home) {
		data.set("rp.home", home.toString());
	}

	@Override
	public double getMoney() {
		return Math.round(data.getDouble("rpmoney", 0.0) * 100) / 100;
	}

	@Override
	public void setMoney(double amount) {
		data.setDouble("rpmoney", amount);
	}

	@Override
	public boolean withdrawMoney(double amount) {
		if (data.getDouble("rpmoney", 0D) >= amount) {
			data.setDouble("rpmoney", data.getDouble("rpmoney", 0D) - amount);
			return true;
		}
		return false;
	}

	@Override
	public void creditMoney(double amount) {
		data.setDouble("rpmoney", data.getDouble("rpmoney", 0D) + amount);
	}

	@Override
	public void setUnpaidTaxes(String city, double amount) {
		if (amount <= 0) {
			data.remove("unpaid." + city);
		} else {
			data.setDouble("unpaid." + city, amount);
		}
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
	}

	@Override
	public Date getLastTaxes(String city) {
		String data = this.data.get("lasttaxes." + city, "none");
		if (data.equals("none")) { return null; }

		String[] parts = data.split("/");
		GregorianCalendar calendar = new GregorianCalendar(Integer.parseInt(parts[2]), Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
		return calendar.getTime();
	}

	@Override
	public Map<String, Double> getUnpaidTaxes() {
		Set<String> dataKeys = data.getKeys();
		return dataKeys.stream()
				.filter(key -> key.startsWith("topay."))
				.collect(Collectors.toMap(key -> key.split("\\.")[1], data::getDouble));
	}

	@Override
	public boolean isNew() {
		return !data.getKeys().contains("rpmoney");
	}

	@Override
	public boolean togglePlotMessages() {
		boolean val = !data.getBoolean("seemessages", true);
		data.setBoolean("seemessages", val);
		return val;
	}
}
