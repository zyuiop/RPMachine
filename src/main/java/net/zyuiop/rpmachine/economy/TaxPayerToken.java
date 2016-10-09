package net.zyuiop.rpmachine.economy;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.LandOwner;

import java.util.UUID;

/**
 * @author zyuiop
 */
public class TaxPayerToken {
	private UUID playerUuid;
	private boolean admin;
	private String cityName;
	private String companyName;

	public TaxPayerToken() {

	}

	public TaxPayer getTaxPayer() {
		if (isAdmin()) {
			return AdminAccountHolder.INSTANCE;
		} else if (playerUuid != null) {
			return RPMachine.database().getPlayerData(playerUuid);
		} else if (cityName != null) {
			return RPMachine.getInstance().getCitiesManager().getCity(cityName);
		} else {
			return null;
		}
	}

	public LandOwner getLandOwner() {
		TaxPayer tp = getTaxPayer();
		return tp != null && tp instanceof LandOwner ? (LandOwner) tp : null;
	}

	public ShopOwner getShopOwner() {
		TaxPayer tp = getTaxPayer();
		return tp != null && tp instanceof ShopOwner ? (ShopOwner) tp : null;
	}

	public UUID getPlayerUuid() {
		return playerUuid;
	}

	public void setPlayerUuid(UUID playerUuid) {
		this.playerUuid = playerUuid;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
}
