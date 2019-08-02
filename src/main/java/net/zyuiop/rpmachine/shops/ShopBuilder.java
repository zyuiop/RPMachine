package net.zyuiop.rpmachine.shops;

import net.zyuiop.rpmachine.entities.RoleToken;
import net.zyuiop.rpmachine.shops.types.AbstractShopSign;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * @author Louis Vialar
 */
public abstract class ShopBuilder<T extends AbstractShopSign> {
    /**
     * This exception can be fired to provide a specific error to the player building the shop
     */
    public static class SignParseError extends RuntimeException {
        public SignParseError(String message) {
            super(message);
        }
    }

    public static class SignPermissionError extends RuntimeException {
        public SignPermissionError(String message) {
            super(message);
        }
    }

    /**
     * Describes the shop format to the player
     *
     * @param player the player to describe the format to
     */
    public abstract void describeFormat(Player player);

    /**
     * Creates an abstract shop sign corresponding to the provided sign
     *
     * @param player the player creating the sign
     * @param lines  the content of the sign (lines 0 to 3)
     * @return an empty optional if the sign is not valid, a shop sign of the sign is valid
     */
    public abstract Optional<T> parseSign(Block block, RoleToken player, String[] lines) throws SignParseError, SignPermissionError;

    /**
     * Check if the player has the permission to put a sign
     * @param player the player to check
     * @return true if it has the permission, false if not
     */
    public boolean hasPermission(RoleToken player) {
        return true;
    }

    /**
     * Send a message to the player to tell them that their shop is ready.
     * @param player the player creating the shop
     */
    public void postCreateInstructions(Player player) {
        player.sendMessage(ChatColor.AQUA + "[" + ChatColor.GREEN + "Shops" + ChatColor.AQUA + "] " + ChatColor.GREEN + "Votre boutique est prête à l'emploi.");
    }

    /**
     * Extracts an integer from a line
     *
     * @param line the line
     * @return the integer
     */
    protected Optional<Integer> extractInt(String line) {
        if (line == null) return Optional.empty();

        try {
            return Optional.of(Integer.parseInt(line));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Extracts a double from a line
     *
     * @param line the line
     * @return the double
     */
    protected Optional<Double> extractPrice(String line) throws SignParseError {
        if (line == null) return Optional.empty();

        try {
            return Optional.of(Double.parseDouble(line)).map(price -> {
                if (price < 0)
                    throw new SignParseError("Le prix ne peut pas être négatif");
                if (price > 100_000_000_000D)
                    throw new SignParseError("Le prix maximal est dépassé (100 milliards)");

                return price;
            });
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
