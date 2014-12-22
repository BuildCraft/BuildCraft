package buildcraft.core;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityBuildCraftItem extends EntityItem {
	public EntityBuildCraftItem(World world, double x, double y, double z, ItemStack stack) {
		super(world, x, y, z, stack);
	}
}
