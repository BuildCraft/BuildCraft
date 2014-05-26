package buildcraft.silicon.boards;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import buildcraft.BuildCraftSilicon;
import buildcraft.api.recipes.IAssemblyRecipe;
import buildcraft.api.transport.PipeWire;

public class BoardRecipe implements IAssemblyRecipe {

	private Object[] inputs;

	public BoardRecipe () {
		inputs = new Object[] {
				new ItemStack(BuildCraftSilicon.redstoneBoard, 1, 0),
				PipeWire.RED.getStack(1),
				PipeWire.BLUE.getStack(1),
				PipeWire.YELLOW.getStack(1),
				PipeWire.GREEN.getStack(1)};
	}

	@Override
	public ItemStack getOutput() {
		return new ItemStack(BuildCraftSilicon.redstoneBoard, 1, 1);
	}

	@Override
	public ItemStack makeOutput() {
		return null;
	}

	@Override
	public Object[] getInputs() {
		return inputs;
	}

	@Override
	public double getEnergyCost() {
		return 10;
	}

	@Override
	public boolean canBeDone(IInventory inv) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void useItems(IInventory inv) {
		// TODO Auto-generated method stub

	}

}
