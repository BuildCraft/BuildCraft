package buildcraft.silicon.recipes;

import buildcraft.BuildCraftTransport;
import buildcraft.api.recipes.IIntegrationRecipeManager;
import buildcraft.api.transport.PipeWire;
import buildcraft.silicon.ItemRedstoneChipset;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.ItemPipeWire;
import buildcraft.transport.TileGenericPipe;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

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
	public ItemStack getOutputForInputs(ItemStack inputA, ItemStack inputB, ItemStack[] components) {
		if (inputA == null || inputB == null) {
			return null;
		}

		if (!(inputA.getItem() instanceof ItemFacade) || !(inputB.getItem() instanceof ItemFacade)) {
			return null;
		}

		PipeWire wire = null;

		for (ItemStack stack : components) {
			if (stack != null && stack.getItem() instanceof ItemPipeWire) {
				wire = PipeWire.fromOrdinal(stack.getItemDamage());
			}
		}

		if (wire == null) {
			return null;
		}

		Block block = ItemFacade.getBlock(inputA);
		Block block_alt = ItemFacade.getBlock(inputB);
		int meta = ItemFacade.getMetaData(inputA);
		int meta_alt = ItemFacade.getMetaData(inputB);

		return ItemFacade.getAdvancedFacade(block, block_alt, meta, meta_alt, wire);
	}

	@Override
	public ItemStack[] getComponents() {
		// Takes any pipe wire and a redstone chipset
		return new ItemStack[]{new ItemStack(BuildCraftTransport.pipeWire, 1, OreDictionary.WILDCARD_VALUE), ItemRedstoneChipset.Chipset.RED.getStack()};
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
