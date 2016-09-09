package buildcraft.lib.misc;

import net.minecraft.block.Block;
import net.minecraft.item.Item;

import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class RegistryUtil {
    public static boolean isRegistered(Block block) {
        return ForgeRegistries.BLOCKS.containsValue(block);
    }

    public static boolean isRegistered(Item item) {
        return ForgeRegistries.ITEMS.containsValue(item);
    }
}
