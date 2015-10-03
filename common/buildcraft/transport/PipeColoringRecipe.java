package buildcraft.transport;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import buildcraft.core.lib.utils.ColorUtils;

public class PipeColoringRecipe implements IRecipe {
	private ItemStack getResult(InventoryCrafting crafting) {
		ItemStack oneColorPipeStack = null;
		ItemStack pipeStack = null;

		boolean hasDifferentPipes = false;

		boolean isBleach = false;
		ItemStack dye = null;

		for (int i = 0; i < 9; i++) {
			ItemStack stack = crafting.getStackInSlot(i);
			if (stack == null || stack.getItem() == null || stack.stackSize == 0) {
				continue;
			}

			if (stack.getItem() instanceof ItemPipe) {
				if (pipeStack == null) {
					pipeStack = new ItemStack(stack.getItem(), 1, 0);
					oneColorPipeStack = new ItemStack(stack.getItem(), 1, stack.getItemDamage());
				} else {
					if (stack.getItem() == pipeStack.getItem()) {
						pipeStack.stackSize++;
						if (oneColorPipeStack.getItemDamage() == oneColorPipeStack.getItemDamage()) {
							oneColorPipeStack.stackSize++;
						}
					} else {
						hasDifferentPipes = true;
					}
				}
			} else if (stack.getItem() == Items.water_bucket) {
				isBleach = true;
			} else if (ColorUtils.isDye(stack)) {
				dye = stack;
			}
		}

		if (isBleach && dye != null) {
			return null;
		} else if (pipeStack != null && (isBleach || (dye != null && pipeStack.stackSize == 8)) && !hasDifferentPipes) {
			ItemStack result = pipeStack;
			if (dye != null) {
				result.setItemDamage(ColorUtils.getColorIDFromDye(dye) + 1);
			}
			return result;
		}

		return null;
	}

	@Override
	public boolean matches(InventoryCrafting crafting, World world) {
		return getResult(crafting) != null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting crafting) {
		return getResult(crafting);
	}

	@Override
	public int getRecipeSize() {
		return 10;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}

}
