package net.zyuiop.rpmachine.shops.types.meta;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Louis Vialar
 */
public class FireworkDataStorage implements ItemStackDataStorage {
    private Set<FireworkEffectStorage> effects;
    private int power;

    @Override
    public String itemName() {
        return "FIREWORK_ROCKET";
    }

    @Override
    public String longItemName() {
        return power + " (avec effets)";
    }

    @Override
    public ItemStack createItemStack(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();

        if (meta instanceof FireworkMeta) {
            FireworkMeta firework = (FireworkMeta) meta;

            firework.clearEffects();
            firework.addEffects(effects.stream().map(FireworkEffectStorage::buildEffect).collect(Collectors.toList()));
            firework.setPower(power);
            stack.setItemMeta(firework);

            return stack;
        }
        return null;
    }

    @Override
    public boolean loadFromItemStack(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();

        if (meta instanceof FireworkMeta) {
            FireworkMeta firework = (FireworkMeta) meta;
            this.effects = firework.getEffects().stream().map(FireworkEffectStorage::new).collect(Collectors.toSet());
            this.power = firework.getPower();
            return true;
        }
        return false;
    }

    @Override
    public boolean isSameItem(ItemStack stack, Material shopMaterial) {
        ItemMeta meta = stack.getItemMeta();

        if (meta instanceof FireworkMeta) {
            FireworkMeta firework = (FireworkMeta) meta;

            return firework.getEffects().stream().map(FireworkEffectStorage::new).collect(Collectors.toSet()).equals(effects) &&
                    power == firework.getPower();
        }
        return false;
    }

    public static class FireworkEffectStorage {
        private Set<Integer> colors;
        private Set<Integer> fadeColors;
        private FireworkEffect.Type type;
        private boolean flicker;
        private boolean trail;

        public FireworkEffectStorage() {}

        FireworkEffectStorage(FireworkEffect effect) {
            this.colors = effect.getColors().stream().map(Color::asRGB).collect(Collectors.toSet());
            this.fadeColors = effect.getFadeColors().stream().map(Color::asRGB).collect(Collectors.toSet());
            this.type = effect.getType();
            this.flicker = effect.hasFlicker();
            this.trail = effect.hasTrail();
        }

        FireworkEffect buildEffect() {
            return FireworkEffect.builder()
                    .with(type)
                    .withColor(colors.stream().map(Color::fromRGB).collect(Collectors.toList()))
                    .withFade(fadeColors.stream().map(Color::fromRGB).collect(Collectors.toList()))
                    .trail(trail)
                    .flicker(flicker)
                    .build();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            FireworkEffectStorage that = (FireworkEffectStorage) o;

            if (flicker != that.flicker) return false;
            if (trail != that.trail) return false;
            if (!Objects.equals(colors, that.colors)) return false;
            if (!Objects.equals(fadeColors, that.fadeColors)) return false;
            return type == that.type;
        }

        @Override
        public int hashCode() {
            int result = colors != null ? colors.hashCode() : 0;
            result = 31 * result + (fadeColors != null ? fadeColors.hashCode() : 0);
            result = 31 * result + (type != null ? type.hashCode() : 0);
            result = 31 * result + (flicker ? 1 : 0);
            result = 31 * result + (trail ? 1 : 0);
            return result;
        }
    }
}
