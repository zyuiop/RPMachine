package net.zyuiop.rpmachine.jobs.restrictions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.zyuiop.rpmachine.RPMachine;
import net.zyuiop.rpmachine.jobs.JobRestriction;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Set;

import static org.bukkit.entity.EntityType.*;

/**
 * @author Louis Vialar
 */
public class CapturingRestriction extends JobRestriction {
    private final String TAG_CAPTURING = "capturing";
    private final ItemStack pokeball;
    private final Set<EntityType> capturableTypes = ImmutableSet.of(
            CAT,
            CHICKEN,
            COW,
            DOLPHIN,
            DONKEY,
            FOX,
            HORSE,
            LLAMA,
            MUSHROOM_COW,
            MULE,
            OCELOT,
            PANDA,
            PARROT,
            PIG,
            POLAR_BEAR,
            RABBIT,
            SHEEP,
            SLIME,
            TURTLE,
            WOLF);

    public CapturingRestriction() {
        ItemStack capturator = new ItemStack(Material.SPAWNER);
        ItemMeta meta = capturator.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "PokeBall");
        meta.setLore(Lists.newArrayList(ChatColor.GRAY + "Clic droit sur un animal pour le capturer !", ChatColor.RED + "Attention !", ChatColor.RED + "Les données de l'entité ne sont pas conservées"));
        capturator.setItemMeta(meta);

        this.pokeball = capturator;

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(RPMachine.getInstance(), "pokeball_craft"), capturator);
        recipe.shape("XXX", "XCX", "XXX");
        recipe.setIngredient('X', Material.IRON_BARS);
        recipe.setIngredient('C', Material.CHEST);
        Bukkit.addRecipe(recipe);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        boolean mainHand = player.getInventory().getItemInOffHand().getType() == Material.AIR;
        ItemStack stack = mainHand ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();

        if (stack.getType() == Material.SPAWNER) {
            if (isAllowed(player)) {
                EntityType type = event.getRightClicked().getType();
                if (capturableTypes.contains(type)) {
                    if (!(event.getRightClicked() instanceof Ageable) || ((Ageable) event.getRightClicked()).isAdult()) {
                        if (event.getRightClicked().getMetadata(TAG_CAPTURING).size() > 0)
                            return;

                        ItemStack egg = new SpawnEgg(type).toItemStack(1);

                        // Check if capture is allowed by target item type
                        if (!RPMachine.getInstance().getJobsManager().isItemAllowed(player, egg.getType())) {
                            player.sendMessage(ChatColor.RED + "Vous ne pouvez pas capturer cette entité avec votre métier actuel.");
                            return;
                        }

                        event.getRightClicked().setMetadata(TAG_CAPTURING, new FixedMetadataValue(RPMachine.getInstance(), "1"));
                        event.getRightClicked().remove();
                        player.sendMessage(ChatColor.GREEN + "Entité capturée !");

                        if (stack.getAmount() > 1)
                            stack.setAmount(stack.getAmount() - 1);
                        else if (mainHand)
                            player.getInventory().setItemInMainHand(null);
                        else player.getInventory().setItemInOffHand(null);

                        player.getInventory().addItem(egg);
                    } else {
                        player.sendMessage(ChatColor.RED + "Impossible de capturer un bébé !");
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Cette entité ne peut pas être capturée.");
                }
            }
        }
    }
}
