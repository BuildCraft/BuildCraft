// STUB(R.Chen): Forge ISpecialArmor — compile shim.
package net.minecraftforge.common;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public interface ISpecialArmor {
    ArmorProperties getProperties(PlayerEntity player, ItemStack armor, Object source, double damage, int slot);

    class ArmorProperties {
        public final int priority;
        public final double ratio;
        public final double absoluteLimit;

        public ArmorProperties(int priority, double ratio, double absoluteLimit) {
            this.priority = priority;
            this.ratio = ratio;
            this.absoluteLimit = absoluteLimit;
        }
    }
}
