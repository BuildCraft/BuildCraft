package buildcraft.api.builder;

import java.util.Random;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.ForgeDirection;

/**
 * A collection of helpful functions to make your life easier.
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class BlueprintHelpers {

	public static final Random RANDOM = new Random();

	/**
	 * Takes a schematic orientation and blueprint orientation and returns the
	 * orientation that should be used in the world. Admittedly this is not
	 * sufficient for 24-point rotation. If you need something more complex, you
	 * will have to handle it yourself.
	 */
	public static ForgeDirection rotateOrientation(ForgeDirection schematicOrientation, ForgeDirection blueprintOrientation) {
		if (schematicOrientation == ForgeDirection.UP || schematicOrientation == ForgeDirection.DOWN) {
			return schematicOrientation;
		}
		if (blueprintOrientation == ForgeDirection.SOUTH) {
			return schematicOrientation.getOpposite();
		}
		if (blueprintOrientation == ForgeDirection.WEST) {
			return schematicOrientation.getRotation(ForgeDirection.DOWN);
		}
		if (blueprintOrientation == ForgeDirection.EAST) {
			return schematicOrientation.getRotation(ForgeDirection.UP);
		}
		return schematicOrientation;
	}

	/**
	 * Takes an ItemStack and uses one. Replaces containers as needed.
	 *
	 * @return the new ItemStack
	 */
	public static ItemStack consumeItem(ItemStack stack) {
		if (stack.stackSize == 1) {
			if (stack.getItem().hasContainerItem()) {
				return stack.getItem().getContainerItemStack(stack);
			} else {
				return null;
			}
		} else {
			stack.splitStack(1);

			return stack;
		}
	}
}
