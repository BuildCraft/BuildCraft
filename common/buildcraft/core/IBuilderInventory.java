package buildcraft.core;

import net.minecraft.src.IInventory;

public interface IBuilderInventory extends IInventory {

	public boolean isBuildingMaterial(int i);

}
