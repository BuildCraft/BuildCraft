/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.boards;

import java.util.ArrayList;
import java.util.HashSet;
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
import buildcraft.api.robots.IDockingStation;
import buildcraft.api.robots.StackRequest;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.robotics.ai.AIRobotCraftAssemblyTable;
import buildcraft.robotics.ai.AIRobotCraftFurnace;
import buildcraft.robotics.ai.AIRobotCraftGeneric;
import buildcraft.robotics.ai.AIRobotCraftWorkbench;
import buildcraft.robotics.ai.AIRobotDeliverRequested;
import buildcraft.robotics.ai.AIRobotDisposeItems;
import buildcraft.robotics.ai.AIRobotGotoSleep;
import buildcraft.robotics.ai.AIRobotGotoStationToUnload;
import buildcraft.robotics.ai.AIRobotSearchStackRequest;
import buildcraft.robotics.ai.AIRobotUnload;
import buildcraft.robotics.statements.ActionRobotFilter;

public class BoardRobotCrafter extends RedstoneBoardRobot {

	private ItemStack order;
	private ArrayList<ItemStack> craftingBlacklist = new ArrayList<ItemStack>();
	private HashSet<IDockingStation> reservedStations = new HashSet<IDockingStation>();
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

		CraftingResult craftingResult = lookForAssemblyTableRecipe(order);

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
		for (Object o : FurnaceRecipes.smelting().getSmeltingList().entrySet()) {
			Map.Entry e = (Map.Entry) o;
			ItemStack input = (ItemStack) e.getKey();
			ItemStack output = (ItemStack) e.getValue();

			if (StackHelper.isMatchingItem(output, order)) {
				return input;
			}
		}

		return null;
	}

	private CraftingResult<?> lookForAssemblyTableRecipe(ItemStack order) {
		for (IFlexibleRecipe r : AssemblyRecipeManager.INSTANCE.getRecipes()) {
			CraftingResult<?> result = r.canCraft(order);

			if (result != null) {
				return result;
			}
		}

		return null;
	}

	private boolean isBlacklisted(ItemStack stack) {
		for (ItemStack black : craftingBlacklist) {
			if (StackHelper.isMatchingItem(stack, black)) {
				return true;
			}
		}

		return false;
	}
}
