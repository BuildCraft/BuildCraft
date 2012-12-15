package buildcraft.core;

import net.minecraft.inventory.IInventory;

public interface IBuilderInventory extends IInventory {

	public boolean isBuildingMaterial(int i);

}
