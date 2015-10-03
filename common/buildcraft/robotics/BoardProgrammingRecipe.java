package buildcraft.robotics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.minecraft.item.ItemStack;

import buildcraft.BuildCraftRobotics;
import buildcraft.api.boards.RedstoneBoardNBT;
import buildcraft.api.boards.RedstoneBoardRegistry;
import buildcraft.api.recipes.IProgrammingRecipe;
import buildcraft.core.lib.utils.NBTUtils;

public class BoardProgrammingRecipe implements IProgrammingRecipe {
	private class BoardSorter implements Comparator<ItemStack> {
		private BoardProgrammingRecipe recipe;

		public BoardSorter(BoardProgrammingRecipe recipe) {
			this.recipe = recipe;
		}

		@Override
		public int compare(ItemStack o1, ItemStack o2) {
			int i = (recipe.getEnergyCost(o1) - recipe.getEnergyCost(o2)) * 200;
			return i != 0 ? i : ItemRedstoneBoard.getBoardNBT(o1).getID().compareTo(ItemRedstoneBoard.getBoardNBT(o2).getID());
		}
	}

	@Override
	public String getId() {
		return "buildcraft:redstone_board";
	}

	@Override
	public List<ItemStack> getOptions(int width, int height) {
		List<ItemStack> options = new ArrayList<ItemStack>(width * height);
		for (RedstoneBoardNBT<?> nbt : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
			ItemStack stack = new ItemStack(BuildCraftRobotics.redstoneBoard);
			nbt.createBoard(NBTUtils.getItemData(stack));
			options.add(stack);
		}
		Collections.sort(options, new BoardSorter(this));
		return options;
	}

	@Override
	public int getEnergyCost(ItemStack option) {
		return RedstoneBoardRegistry.instance.getEnergyCost(
				RedstoneBoardRegistry.instance.getRedstoneBoard(option.getTagCompound().getString("id"))
		);
	}

	@Override
	public boolean canCraft(ItemStack input) {
		return input.getItem() instanceof ItemRedstoneBoard;
	}

	@Override
	public ItemStack craft(ItemStack input, ItemStack option) {
		return option.copy();
	}
}
