package buildcraft.lib.item;

import net.minecraft.item.ItemStack;

public class ItemStackHelper {

    public static boolean isEmpty(ItemStack stack) {
        return stack == null || stack.stackSize <= 0 || stack.getItemDamage() < -32768 || stack.getItemDamage() > 65535;
    }
}
