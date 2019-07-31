package net.zyuiop.rpmachine.economy;

import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.cities.LandOwner;
import net.zyuiop.rpmachine.cities.data.City;
import net.zyuiop.rpmachine.database.PlayerData;
import net.zyuiop.rpmachine.permissions.DelegatedPermission;
import net.zyuiop.rpmachine.projects.Project;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

/**
 * @author zyuiop
 */
public class TaxPayerToken {
	private UUID playerUuid;
	private boolean admin;
	private String cityName;
	private String companyName;
	private String projectName;

	public TaxPayerToken() {

	}

	public TaxPayer getTaxPayer() {
		if (isAdmin()) {
			return AdminTaxPayer.INSTANCE;
		} else if (playerUuid != null) {
			return RPMachine.database().getPlayerData(playerUuid);
		} else if (cityName != null) {
			return RPMachine.getInstance().getCitiesManager().getCity(cityName);
		} else if (projectName != null) {
			return RPMachine.getInstance().getProjectsManager().getZone(projectName);
		} else {
			return null;
		}
	}

	/**
	 * Check if the given player has the right to execute the given command as the current taxpayer
	 * @param player the player to check
	 * @param permission the permission to check
	 * @return true if the command is allowed, false if not
	 */
	public boolean hasDelegatedPermission(@Nonnull Player player, @Nonnull DelegatedPermission permission) {
		Validate.notNull(player);
		Validate.notNull(permission);

		return getTaxPayer().hasDelegatedPermission(player, permission);
	}

	public boolean checkDelegatedPermission(@Nonnull Player player, @Nonnull DelegatedPermission permission) {
		if (!hasDelegatedPermission(player, permission)) {
			player.sendMessage(ChatColor.RED + "Vous n'avez pas le droit de faire ça en tant que " + displayable());
			return false;
		}

		return true;
	}

	public String displayable() {
		if (isAdmin()) {
			return ChatColor.RED + "La Confédération";
		} else if (playerUuid != null) {
			return ChatColor.YELLOW + "(Joueur) " + ChatColor.GOLD + RPMachine.database().getUUIDTranslator().getName(playerUuid);
		} else if (cityName != null) {
			City city = RPMachine.getInstance().getCitiesManager().getCity(cityName);
			if (city == null) {
				return ChatColor.RED + "Ville supprimée";
			} else {
				return ChatColor.AQUA + "(Ville) " + ChatColor.DARK_AQUA + city.getCityName();
			}
		} else if (projectName != null) {
			Project project = RPMachine.getInstance().getProjectsManager().getZone(projectName);
			if (project == null) {
				return ChatColor.RED + "Projet supprimé";
			} else {
				return ChatColor.GREEN + "(Projet) " + ChatColor.DARK_GREEN + project.getPlotName();
			}
		}

		return ChatColor.DARK_RED + "????";
	}

	public String shortDisplayable() {
		if (isAdmin()) {
			return ChatColor.RED + "Confédération";
		} else if (playerUuid != null) {
			return ChatColor.GOLD + RPMachine.database().getUUIDTranslator().getName(playerUuid);
		} else if (cityName != null) {
			City city = RPMachine.getInstance().getCitiesManager().getCity(cityName);
			if (city == null) {
				return ChatColor.RED + "Ville";
			} else {
				return ChatColor.DARK_AQUA + city.getCityName();
			}
		} else if (projectName != null) {
			Project project = RPMachine.getInstance().getProjectsManager().getZone(projectName);
			if (project == null) {
				return ChatColor.RED + "Projet";
			} else {
				return ChatColor.DARK_GREEN + project.getPlotName();
			}
		}

		return null;
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

	public static TaxPayerToken fromPayer(TaxPayer owner) {
		TaxPayerToken tp = new TaxPayerToken();
		if (owner instanceof PlayerData)
			tp.setPlayerUuid(((PlayerData) owner).getUuid());
		else if (owner instanceof AdminTaxPayer)
			tp.setAdmin(true);
		else if (owner instanceof City)
			tp.setCityName(((City) owner).getCityName());
		else if (owner instanceof Project)
			tp.setProjectName(((Project) owner).getPlotName());

		return tp;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof TaxPayerToken)) return false;

		TaxPayerToken token = (TaxPayerToken) o;

		if (admin != token.admin) return false;
		if (!Objects.equals(playerUuid, token.playerUuid)) return false;
		if (!Objects.equals(cityName, token.cityName)) return false;
		return Objects.equals(companyName, token.companyName);
	}

	@Override
	public int hashCode() {
		int result = playerUuid != null ? playerUuid.hashCode() : 0;
		result = 31 * result + (admin ? 1 : 0);
		result = 31 * result + (cityName != null ? cityName.hashCode() : 0);
		result = 31 * result + (companyName != null ? companyName.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "TaxPayerToken{" +
				"playerUuid=" + playerUuid +
				", admin=" + admin +
				", cityName='" + cityName + '\'' +
				", companyName='" + companyName + '\'' +
				'}';
	}
}
