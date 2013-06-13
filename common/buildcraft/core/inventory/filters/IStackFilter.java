package buildcraft.core.inventory.filters;

import net.minecraft.item.ItemStack;

/**
 * This interface provides a convenient means of dealing with entire classes of
 * items without having to specify each item individually.
 *
 * @author CovertJaguar <http://www.railcraft.info>
 */
public interface IStackFilter {

	public boolean matches(ItemStack stack);
}
