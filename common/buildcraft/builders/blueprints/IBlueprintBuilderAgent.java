package buildcraft.builders.blueprints;

import net.minecraft.inventory.IInventory;

public interface IBlueprintBuilderAgent {

	public boolean breakBlock (int x, int y, int z);

	public IInventory getInventory ();

	public boolean buildBlock(int x, int y, int z);

}
