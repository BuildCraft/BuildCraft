/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots.boards;

import java.util.ArrayList;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IFlexibleRecipe;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.StackRequest;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.core.robots.AIRobotCraftAssemblyTable;
import buildcraft.core.robots.AIRobotCraftFurnace;
import buildcraft.core.robots.AIRobotCraftGeneric;
import buildcraft.core.robots.AIRobotCraftWorkbench;
import buildcraft.core.robots.AIRobotDeliverRequested;
import buildcraft.core.robots.AIRobotDisposeItems;
import buildcraft.core.robots.AIRobotGotoSleep;
import buildcraft.core.robots.AIRobotGotoStationToUnload;
import buildcraft.core.robots.AIRobotSearchStackRequest;
import buildcraft.core.robots.AIRobotUnload;
import buildcraft.silicon.statements.ActionRobotFilter;

public class BoardRobotCrafter extends RedstoneBoardRobot {

	private ItemStack order;
	private ArrayList<ItemStack> craftingBlacklist = new ArrayList<ItemStack>();
	// TOOD: remove this if necessary
//	private HashSet<IDockingStation> reservedStations = new HashSet<IDockingStation>();
	private StackRequest currentRequest = null;

	public BoardRobotCrafter(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotCrafterNBT.instance;
	}

	@Override
	public void update() {
		if (robot.containsItems()) {
			// Always makes sure that when starting a craft, the inventory is
			// clean.

			startDelegateAI(new AIRobotDisposeItems(robot));
			return;
		}

		if (currentRequest != null) {
			order = currentRequest.stack;
		} else {
			order = null;
		}

		if (order == null) {
			startDelegateAI(new AIRobotSearchStackRequest(robot, ActionRobotFilter.getGateFilter(robot
					.getLinkedStation()), craftingBlacklist));
			return;
		}

		IRecipe recipe = lookForWorkbenchRecipe(order);

		if (recipe != null) {
			startDelegateAI(new AIRobotCraftWorkbench(robot, recipe));
			return;
		}

		ItemStack furnaceInput = lookForFurnaceRecipe(order);

		if (furnaceInput != null) {
			startDelegateAI(new AIRobotCraftFurnace(robot, furnaceInput, order));
			return;
		}

		CraftingResult<ItemStack> craftingResult = lookForAssemblyTableRecipe(order);

		if (craftingResult != null) {
			startDelegateAI(new AIRobotCraftAssemblyTable(robot, craftingResult));
			return;
		}

		craftingBlacklist.add(order);
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotCraftGeneric) {
			if (!ai.success()) {
				robot.releaseResources();
				currentRequest = null;
				craftingBlacklist.add(order);
			} else {
				if (currentRequest != null) {
					startDelegateAI(new AIRobotDeliverRequested(robot, currentRequest));
				} else {
					robot.releaseResources();
					// The extra crafted items may make some crafting possible
					craftingBlacklist.clear();
				}
			}
		} else if (ai instanceof AIRobotGotoStationToUnload) {
			if (ai.success()) {
				startDelegateAI(new AIRobotUnload(robot));
			} else {
				robot.releaseResources();
				currentRequest = null;
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		} else if (ai instanceof AIRobotSearchStackRequest) {
			if (!ai.success()) {
				craftingBlacklist.clear();
				currentRequest = null;
				startDelegateAI(new AIRobotGotoSleep(robot));
			} else {
				currentRequest = ((AIRobotSearchStackRequest) ai).request;

				if (!currentRequest.station.take(robot)) {
					currentRequest = null;
				}
			}
		} else if (ai instanceof AIRobotDeliverRequested) {
			currentRequest = null;
			robot.releaseResources();
			// The extra crafted items may make some crafting possible
			craftingBlacklist.clear();
		}
	}

	private IRecipe lookForWorkbenchRecipe(ItemStack order) {
		for (Object o : CraftingManager.getInstance().getRecipeList()) {
			IRecipe r = (IRecipe) o;

			if (r instanceof ShapedRecipes
					|| r instanceof ShapelessRecipes
					|| r instanceof ShapedOreRecipe
					|| r instanceof ShapelessOreRecipe) {
				if (StackHelper.isMatchingItem(r.getRecipeOutput(), order)) {
					return r;
				}
			}
		}

		return null;
	}

	private ItemStack lookForFurnaceRecipe(ItemStack order) {
		for (Object o : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
			@SuppressWarnings("unchecked")
			Map.Entry<ItemStack, ItemStack> e = (Map.Entry<ItemStack, ItemStack>) o;
			ItemStack input = e.getKey();
			ItemStack output =  e.getValue();

			if (StackHelper.isMatchingItem(output, order)) {
				return input;
			}
		}

		return null;
	}

	private CraftingResult<ItemStack> lookForAssemblyTableRecipe(ItemStack order) {
		for (IFlexibleRecipe<ItemStack> r : AssemblyRecipeManager.INSTANCE.getRecipes()) {
			CraftingResult<ItemStack> result = r.canCraft(order);

			if (result != null) {
				return result;
			}
		}

		return null;
	}

	// TODO: Remove this if necessary
//	private boolean isBlacklisted(ItemStack stack) {
//		for (ItemStack black : craftingBlacklist) {
//			if (StackHelper.isMatchingItem(stack, black)) {
//				return true;
//			}
//		}
//
//		return false;
//	}
}
