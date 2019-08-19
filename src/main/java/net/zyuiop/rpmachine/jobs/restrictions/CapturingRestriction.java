package net.zyuiop.rpmachine.jobs.restrictions;

import com.google.common.collect.ImmutableMap;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Map;

/**
 * @author Louis Vialar
 */
public class CapturingRestriction extends JobRestriction {
    private final String TAG_CAPTURING = "capturing";
    private final Map<EntityType, Material> SPAWN_EGGS = ImmutableMap.<EntityType, Material>builder()
            .put(EntityType.BAT, Material.BAT_SPAWN_EGG)
            .put(EntityType.BLAZE, Material.BLAZE_SPAWN_EGG)
            .put(EntityType.CAT, Material.CAT_SPAWN_EGG)
            .put(EntityType.CAVE_SPIDER, Material.CAVE_SPIDER_SPAWN_EGG)
            .put(EntityType.CHICKEN, Material.CHICKEN_SPAWN_EGG)
            .put(EntityType.COD, Material.COD_SPAWN_EGG)
            .put(EntityType.COW, Material.COW_SPAWN_EGG)
            .put(EntityType.CREEPER, Material.CREEPER_SPAWN_EGG)
            .put(EntityType.DOLPHIN, Material.DOLPHIN_SPAWN_EGG)
            .put(EntityType.DONKEY, Material.DONKEY_SPAWN_EGG)
            .put(EntityType.DROWNED, Material.DROWNED_SPAWN_EGG)
            .put(EntityType.ELDER_GUARDIAN, Material.ELDER_GUARDIAN_SPAWN_EGG)
            .put(EntityType.ENDERMAN, Material.ENDERMAN_SPAWN_EGG)
            .put(EntityType.ENDERMITE, Material.ENDERMITE_SPAWN_EGG)
            .put(EntityType.EVOKER, Material.EVOKER_SPAWN_EGG)
            .put(EntityType.FOX, Material.FOX_SPAWN_EGG)
            .put(EntityType.GHAST, Material.GHAST_SPAWN_EGG)
            .put(EntityType.GUARDIAN, Material.GUARDIAN_SPAWN_EGG)
            .put(EntityType.HORSE, Material.HORSE_SPAWN_EGG)
            .put(EntityType.HUSK, Material.HUSK_SPAWN_EGG)
            .put(EntityType.LLAMA, Material.LLAMA_SPAWN_EGG)
            .put(EntityType.MAGMA_CUBE, Material.MAGMA_CUBE_SPAWN_EGG)
            .put(EntityType.MUSHROOM_COW, Material.MOOSHROOM_SPAWN_EGG)
            .put(EntityType.MULE, Material.MULE_SPAWN_EGG)
            .put(EntityType.OCELOT, Material.OCELOT_SPAWN_EGG)
            .put(EntityType.PANDA, Material.PANDA_SPAWN_EGG)
            .put(EntityType.PARROT, Material.PARROT_SPAWN_EGG)
            .put(EntityType.PHANTOM, Material.PHANTOM_SPAWN_EGG)
            .put(EntityType.PIG, Material.PIG_SPAWN_EGG)
            .put(EntityType.PILLAGER, Material.PILLAGER_SPAWN_EGG)
            .put(EntityType.POLAR_BEAR, Material.POLAR_BEAR_SPAWN_EGG)
            .put(EntityType.PUFFERFISH, Material.PUFFERFISH_SPAWN_EGG)
            .put(EntityType.RABBIT, Material.RABBIT_SPAWN_EGG)
            .put(EntityType.RAVAGER, Material.RAVAGER_SPAWN_EGG)
            .put(EntityType.SALMON, Material.SALMON_SPAWN_EGG)
            .put(EntityType.SHEEP, Material.SHEEP_SPAWN_EGG)
            .put(EntityType.SHULKER, Material.SHULKER_SPAWN_EGG)
            .put(EntityType.SILVERFISH, Material.SILVERFISH_SPAWN_EGG)
            .put(EntityType.SKELETON_HORSE, Material.SKELETON_HORSE_SPAWN_EGG)
            .put(EntityType.SKELETON, Material.SKELETON_SPAWN_EGG)
            .put(EntityType.SLIME, Material.SLIME_SPAWN_EGG)
            .put(EntityType.SPIDER, Material.SPIDER_SPAWN_EGG)
            .put(EntityType.SQUID, Material.SQUID_SPAWN_EGG)
            .put(EntityType.STRAY, Material.STRAY_SPAWN_EGG)
            .put(EntityType.TRADER_LLAMA, Material.TRADER_LLAMA_SPAWN_EGG)
            .put(EntityType.TROPICAL_FISH, Material.TROPICAL_FISH_SPAWN_EGG)
            .put(EntityType.TURTLE, Material.TURTLE_SPAWN_EGG)
            .put(EntityType.VEX, Material.VEX_SPAWN_EGG)
            .put(EntityType.VILLAGER, Material.VILLAGER_SPAWN_EGG)
            .put(EntityType.VINDICATOR, Material.VINDICATOR_SPAWN_EGG)
            .put(EntityType.WANDERING_TRADER, Material.WANDERING_TRADER_SPAWN_EGG)
            .put(EntityType.WITCH, Material.WITCH_SPAWN_EGG)
            .put(EntityType.WITHER_SKELETON, Material.WITHER_SKELETON_SPAWN_EGG)
            .put(EntityType.WOLF, Material.WOLF_SPAWN_EGG)
            .put(EntityType.ZOMBIE_HORSE, Material.ZOMBIE_HORSE_SPAWN_EGG)
            .put(EntityType.PIG_ZOMBIE, Material.ZOMBIE_PIGMAN_SPAWN_EGG)
            .put(EntityType.ZOMBIE, Material.ZOMBIE_SPAWN_EGG)
            .put(EntityType.ZOMBIE_VILLAGER, Material.ZOMBIE_VILLAGER_SPAWN_EGG)
            .build();

    public CapturingRestriction() {
        ItemStack capturator = new ItemStack(Material.SPAWNER);
        ItemMeta meta = capturator.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "PokeBall");
        meta.setLore(Lists.newArrayList(ChatColor.GRAY + "Clic droit sur un animal pour le capturer !", ChatColor.RED + "Attention !", ChatColor.RED + "Les données de l'entité ne sont pas conservées"));
        capturator.setItemMeta(meta);

        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(RPMachine.getInstance(), "pokeball_craft"), capturator);
        recipe.shape("XXX", "XCX", "XXX");
        recipe.setIngredient('X', Material.IRON_BARS);
        recipe.setIngredient('C', Material.CHEST);
        Bukkit.addRecipe(recipe);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        boolean mainHand = event.getHand() != EquipmentSlot.OFF_HAND;
        ItemStack stack = mainHand ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();

        if (stack.getType() == Material.SPAWNER) {
            if (isAllowed(player)) {
                EntityType type = event.getRightClicked().getType();
                if (SPAWN_EGGS.containsKey(type)) {
                    if (!(event.getRightClicked() instanceof Ageable) || ((Ageable) event.getRightClicked()).isAdult()) {
                        if (event.getRightClicked().getMetadata(TAG_CAPTURING).size() > 0)
                            return;

                        ItemStack egg = new ItemStack(SPAWN_EGGS.get(type));

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
