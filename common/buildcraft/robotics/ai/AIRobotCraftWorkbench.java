/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.ai;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.inventory.ITransactor;
import buildcraft.core.lib.inventory.InventoryCopy;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.core.lib.inventory.Transactor;
import buildcraft.core.lib.inventory.filters.ArrayStackFilter;
import buildcraft.core.lib.inventory.filters.ArrayStackOrListFilter;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.robotics.DockingStation;
import buildcraft.robotics.IStationFilter;
import buildcraft.robotics.statements.ActionRobotFilter;
import buildcraft.robotics.statements.ActionStationAllowCraft;

public class AIRobotCraftWorkbench extends AIRobotCraftGeneric {

	private IRecipe recipe;
	private int craftingTimer = 0;
	private ArrayList<ArrayStackFilter> requirements;
	private ItemStack output;

	public AIRobotCraftWorkbench(EntityRobotBase iRobot) {
		super(iRobot);
	}

	public AIRobotCraftWorkbench(EntityRobotBase iRobot, IRecipe iRecipe) {
		super(iRobot);

		recipe = iRecipe;
	}

	@Override
	public void start () {
		requirements = tryCraft(false);

		if (requirements == null) {
			terminate ();
			return;
		}
	}

	@Override
	public void update() {
		if (recipe == null) {
			// loading error
			terminate();
			return;
		}

		// [1] look for a crafting order
		// -- if none, clear temporary item blacklist and sleep
		// [2] look and fetch items needed to craft (problem with 9 slots inv?)
		// -- allow upgrades!! And to show contents
		// -- if can't be done, add item to temporary blacklist, drop inv either
		// -- in a inventory accepting items or drop in the world, then look for
		// -- another order
		// [3] look and goto a station next to a workbench, craft
		// -- if not, sleep

		if (craftingTimer > 0) {
			craftingTimer--;

			if (craftingTimer == 0) {
				craft();
				terminate();
			}
		} else if (requirements.size() > 0) {
			startDelegateAI(new AIRobotGotoStationToLoad(robot, new ReqStackFilter(), robot.getZoneToWork()));
		} else {
			startDelegateAI(new AIRobotSearchAndGotoStation(robot, new StationWorkbenchFilter(), robot.getZoneToWork()));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotGotoStationToLoad) {
			if (ai.success()) {
				startDelegateAI(new AIRobotLoad(robot, new ReqStackFilter(), 1));
			} else {
				terminate();
			}
		} else if (ai instanceof AIRobotLoad) {
			requirements = tryCraft(false);
		} else if (ai instanceof AIRobotSearchAndGotoStation) {
			if (new StationWorkbenchFilter().matches((DockingStation) robot.getDockingStation())) {
				craftingTimer = 40;
			} else {
				terminate();
			}
		}
	}

	protected ArrayList<ArrayStackFilter> tryCraft(boolean doRemove) {
		Object[] items = new Object[0];

		if (recipe instanceof ShapedRecipes) {
			items = ((ShapedRecipes) recipe).recipeItems;
		} else if (recipe instanceof ShapelessRecipes) {
			items = ((ShapelessRecipes) recipe).recipeItems.toArray();
		} else if (recipe instanceof ShapedOreRecipe) {
			items = ((ShapedOreRecipe) recipe).getInput();
		} else if (recipe instanceof ShapelessOreRecipe) {
			items = ((ShapelessOreRecipe) recipe).getInput().toArray();
		}

		ArrayList<ArrayStackFilter> result = new ArrayList<ArrayStackFilter>();

		IInventory inv = robot;

		if (!doRemove) {
			inv = new InventoryCopy(robot);
		}

		InventoryCrafting invCraft = new InventoryCrafting(new Container() {

			@Override
			public boolean canInteractWith(EntityPlayer player) {
				// TODO Auto-generated method stub
				return false;
			}
		}, 3, 3);

		for (int i = 0; i < items.length; ++i) {
			Object tmp = items [i];

			if (tmp == null) {
				continue;
			}

			int qty = 0;
			ArrayStackFilter filter;

			if (tmp instanceof ItemStack) {
				ItemStack stack = (ItemStack) tmp;
				qty = stack.stackSize;
				filter = new ArrayStackFilter(stack);
			} else {
				ArrayList<ItemStack> stacks = (ArrayList<ItemStack>) tmp;
				qty = stacks.get(0).stackSize;
				filter = new ArrayStackFilter(stacks.toArray(new ItemStack[stacks.size()]));
			}

			for (IInvSlot s : InventoryIterator.getIterable(inv)) {
				if (filter.matches(s.getStackInSlot())) {
					ItemStack removed = s.decreaseStackInSlot(qty);

					qty = qty - removed.stackSize;

					if (invCraft.getStackInSlot(i) != null) {
						invCraft.getStackInSlot(i).stackSize += qty;
					} else {
						invCraft.setInventorySlotContents(i, removed);
					}

					invCraft.setInventorySlotContents(i, removed);

					if (removed.stackSize == 0) {
						break;
					}
				}
			}

			if (qty > 0) {
				result.add(filter);
			}
		}

		if (result.size() == 0 && doRemove) {
			output = recipe.getCraftingResult(invCraft);
		}

		return result;
	}

	private void craft() {
		if (tryCraft(true).size() == 0 && output != null) {
			crafted = true;

			ITransactor transactor = Transactor.getTransactorFor(robot);

			transactor.add(output, ForgeDirection.UNKNOWN, true);
		}
	}

	private class ReqStackFilter implements IStackFilter {
		@Override
		public boolean matches(ItemStack stack) {
			for (ArrayStackFilter s : requirements) {
				if (s.matches(stack)) {
					return true;
				}
			}

			return false;
		}
	}

	private class StationWorkbenchFilter implements IStationFilter {

		@Override
		public boolean matches(DockingStation station) {
			if (!ActionRobotFilter.canInteractWithItem(station, new ArrayStackOrListFilter(recipe.getRecipeOutput()),
					ActionStationAllowCraft.class)) {
				return false;
			}

			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				Block nearbyBlock = robot.worldObj.getBlock(station.x() + dir.offsetX, station.y()
						+ dir.offsetY, station.z()
						+ dir.offsetZ);

				if (nearbyBlock instanceof BlockWorkbench) {
					return true;
				}
			}

			return false;
		}
	}

	@Override
	public int getEnergyCost() {
		return 30;
	}
}
