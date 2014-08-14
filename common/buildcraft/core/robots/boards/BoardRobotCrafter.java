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
import java.util.HashSet;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.gates.ActionParameterItemStack;
import buildcraft.api.gates.IActionParameter;
import buildcraft.api.gates.IStatementParameter;
import buildcraft.api.recipes.CraftingResult;
import buildcraft.api.recipes.IFlexibleRecipe;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IDockingStation;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.robots.StackRequest;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.recipes.AssemblyRecipeManager;
import buildcraft.core.robots.AIRobotCraftAssemblyTable;
import buildcraft.core.robots.AIRobotCraftFurnace;
import buildcraft.core.robots.AIRobotCraftGeneric;
import buildcraft.core.robots.AIRobotCraftWorkbench;
import buildcraft.core.robots.AIRobotDeliverRequested;
import buildcraft.core.robots.AIRobotGotoSleep;
import buildcraft.core.robots.AIRobotGotoStationToUnload;
import buildcraft.core.robots.AIRobotSearchStation;
import buildcraft.core.robots.AIRobotUnload;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.robots.IStationFilter;
import buildcraft.silicon.statements.ActionRobotCraft;
import buildcraft.silicon.statements.ActionStationRequestItems;
import buildcraft.silicon.statements.ActionStationRequestItemsMachine;
import buildcraft.transport.Pipe;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.gates.ActionSlot;

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

			// TODO: We should call load or drop, in order to clean items even
			// if no destination is to be found
			startDelegateAI(new AIRobotGotoStationToUnload(robot, robot.getZoneToWork()));
			return;
		}

		if (currentRequest == null) {
			order = getOrderFromHomeStation();
		} else {
			order = currentRequest.stack;
		}

		if (order == null) {
			startDelegateAI(new AIRobotSearchStation(robot, new StationProviderFilter(), robot.getZoneToWork()));
			return;
		}

		IRecipe recipe = lookForWorkbenchRecipe(order);

		if (recipe != null) {
			startDelegateAI(new AIRobotCraftWorkbench(robot, recipe));
			return;
		}

		ItemStack furnaceInput = lookForFurnaceRecipe(order);

		if (furnaceInput != null) {
			startDelegateAI(new AIRobotCraftFurnace(robot, furnaceInput));
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
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		} else if (ai instanceof AIRobotSearchStation) {
			if (!ai.success()) {
				craftingBlacklist.clear();
				startDelegateAI(new AIRobotGotoSleep(robot));
			} else {
				currentRequest = getOrderFromRequestingAction(((AIRobotSearchStation) ai).targetStation);

				if (currentRequest == null) {
					currentRequest = getOrderFromRequestingStation(((AIRobotSearchStation) ai).targetStation, true);
				}

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

	private ItemStack getOrderFromHomeStation() {
		DockingStation s = (DockingStation) robot.getLinkedStation();

		for (ActionSlot slot : new ActionIterator(s.getPipe().pipe)) {
			if (slot.action instanceof ActionRobotCraft) {
				for (IActionParameter p : slot.parameters) {
					if (p != null && p instanceof ActionParameterItemStack) {
						ActionParameterItemStack param = (ActionParameterItemStack) p;
						ItemStack stack = param.getItemStackToDraw();

						if (stack != null && !isBlacklisted(stack)) {
							return stack;
						}
					}
				}
			}
		}

		return null;
	}

	private StackRequest getOrderFromRequestingStation(DockingStation station, boolean take) {
		boolean actionFound = false;

		Pipe pipe = station.getPipe().pipe;

		for (ActionSlot s : new ActionIterator(pipe)) {
			if (s.action instanceof ActionStationRequestItemsMachine) {
				actionFound = true;
			}
		}

		if (!actionFound) {
			return null;
		}

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			TileEntity nearbyTile = robot.worldObj.getTileEntity(station.x() + dir.offsetX, station.y()
					+ dir.offsetY, station.z()
					+ dir.offsetZ);

			if (nearbyTile instanceof IRequestProvider) {
				IRequestProvider provider = (IRequestProvider) nearbyTile;

				for (int i = 0; i < provider.getNumberOfRequests(); ++i) {
					StackRequest request = provider.getAvailableRequest(i);

					if (request != null && !isBlacklisted(request.stack)) {
						request.station = station;

						if (take) {
							if (provider.takeRequest(i, robot)) {
								return request;
							}
						} else {
							return request;
						}
					}
				}
			}
		}

		return null;
	}

	private StackRequest getOrderFromRequestingAction(DockingStation station) {
		boolean actionFound = false;

		Pipe pipe = station.getPipe().pipe;

		for (ActionSlot s : new ActionIterator(pipe)) {
			if (s.action instanceof ActionStationRequestItems) {
				for (IStatementParameter p : s.parameters) {
					ActionParameterItemStack param = (ActionParameterItemStack) p;

					if (param != null && !isBlacklisted(param.getItemStackToDraw())) {
						StackRequest req = new StackRequest();
						req.station = station;
						req.stack = param.getItemStackToDraw();

						return req;
					}
				}
			}
		}

		return null;
	}

	private class StationProviderFilter implements IStationFilter {

		@Override
		public boolean matches(DockingStation station) {
			return getOrderFromRequestingAction(station) != null
					|| getOrderFromRequestingStation(station, false) != null;
		}
	}
}
