package net.zyuiop.rpmachine.economy;

import net.md_5.bungee.api.ChatColor;

public enum Messages {
	ECO_PREFIX(ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "Économie" + ChatColor.DARK_AQUA + "] " + ChatColor.RESET),
	SHOPS_PREFIX(ChatColor.GOLD + "[" + ChatColor.YELLOW + "Boutiques" + ChatColor.GOLD + "] " + ChatColor.RESET),
	NOT_ENOUGH_MONEY(Messages.ECO_PREFIX.getMessage() + "" + ChatColor.RED + "Vous n'avez pas assez d'argent pour faire cela."),
	SENT_MONEY(Messages.ECO_PREFIX.getMessage() + "" + ChatColor.GREEN + "Vous avez envoyé " + ChatColor.YELLOW + "{AMT} $"),
	RECEIVED_MONEY(Messages.ECO_PREFIX.getMessage() + "" + ChatColor.GREEN + "Vous avez reçu " + ChatColor.YELLOW + "{AMT} $ " + ChatColor.GREEN + " de la part de " + ChatColor.YELLOW + "{FROM}"),
	AMOUNT_MESSAGE(ECO_PREFIX.getMessage() + "" + ChatColor.YELLOW + "Vous avez actuellement " + ChatColor.GOLD + "{AMT} $");
	;

	private final String message;

	public String getMessage() {
		return message;
	}

	private Messages(String message) {
		this.message = message;
	}

}
