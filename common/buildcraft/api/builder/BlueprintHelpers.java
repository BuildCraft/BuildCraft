package buildcraft.api.builder;

import net.minecraftforge.common.ForgeDirection;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public class BlueprintHelpers {

	/**
	 * Takes a schematic orientation and blueprint orientation and returns the
	 * orientation that should be used in the world. Admittedly this is not
	 * sufficient for 24-point rotation. If you need something more complex,
	 * you will have to handle it yourself.
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
}
