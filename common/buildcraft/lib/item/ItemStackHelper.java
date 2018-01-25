package buildcraft.lib.item;

import net.minecraft.item.ItemStack;

public class ItemStackHelper {

    public static boolean isEmpty(ItemStack stack) {
        if (stack == null)
            return true;
        else if (stack.stackSize <= 0)
            return true;
        else return stack.isItemStackDamageable() && (stack.getItemDamage() < -32768 || stack.getItemDamage() > 65535);
    }
}
