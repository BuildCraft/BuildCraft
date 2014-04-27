package buildcraft.silicon.recipes;

import buildcraft.api.recipes.IIntegrationRecipeManager;
import buildcraft.api.transport.PipeWire;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.TileGenericPipe;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class AdvancedFacadeRecipe implements IIntegrationRecipeManager.IIntegrationRecipe {

	@Override
	public double getEnergyCost() {
		return 5000;
	}

	@Override
	public boolean isValidInputA(ItemStack inputA) {
		return inputA != null && inputA.getItem() instanceof ItemFacade && ItemFacade.getType(inputA) == TileGenericPipe.FACADE_BASIC;
	}

	@Override
	public boolean isValidInputB(ItemStack inputB) {
		return inputB != null && inputB.getItem() instanceof ItemFacade && ItemFacade.getType(inputB) == TileGenericPipe.FACADE_BASIC;
	}

	@Override
	public ItemStack getOutputForInputs(ItemStack inputA, ItemStack inputB) {
		if (inputA == null || inputB == null) {
			return null;
		}

		if (!(inputA.getItem() instanceof ItemFacade) || !(inputB.getItem() instanceof ItemFacade)) {
			return null;
		}

		Block block = ItemFacade.getBlock(inputA);
		Block block_alt = ItemFacade.getBlock(inputB);
		int meta = ItemFacade.getMetaData(inputA);
		int meta_alt = ItemFacade.getMetaData(inputB);

		//TODO Pipe wire definition
		return ItemFacade.getAdvancedFacade(block, block_alt, meta, meta_alt, PipeWire.RED);
	}

	@Override
	public ItemStack[] getExampleInputsA() {
		return new ItemStack[0];
	}

	@Override
	public ItemStack[] getExampleInputsB() {
		return new ItemStack[0];
	}

}
