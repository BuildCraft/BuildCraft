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

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.api.boards.RedstoneBoardRobot;
import buildcraft.api.boards.RedstoneBoardRobotNBT;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.BuildCraftAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.robots.AIRobotGotoBlock;
import buildcraft.core.robots.AIRobotGotoSleep;
import buildcraft.core.robots.AIRobotGotoStationAndUnloadFluids;
import buildcraft.core.robots.AIRobotPumpBlock;
import buildcraft.core.robots.AIRobotSearchBlock;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.robots.IBlockFilter;
import buildcraft.core.robots.ResourceIdBlock;
import buildcraft.silicon.statements.ActionRobotFilter;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.gates.StatementSlot;

public class BoardRobotPump extends RedstoneBoardRobot {

	private BlockIndex blockFound;
	private ArrayList<Fluid> fluidFilter = new ArrayList<Fluid>();

	public BoardRobotPump(EntityRobotBase iRobot) {
		super(iRobot);
	}

	@Override
	public RedstoneBoardRobotNBT getNBTHandler() {
		return BoardRobotPumpNBT.instance;
	}

	@Override
	public void update() {
		FluidStack tank = robot.getTankInfo(EnumFacing.UNKNOWN)[0].fluid;

		if (tank != null && tank.amount > 0) {
			startDelegateAI(new AIRobotGotoStationAndUnloadFluids(robot, robot.getZoneToWork()));
		} else {
			updateFilter();

			startDelegateAI(new AIRobotSearchBlock(robot, new IBlockFilter() {

				@Override
				public boolean matches(World world, int x, int y, int z) {
					if (BuildCraftAPI.isFluidSource.get(world, x, y, z)
							&& !robot.getRegistry().isTaken(new ResourceIdBlock(x, y, z))) {
						return matchesGateFilter(world, x, y, z);
					} else {
						return false;
					}
				}
			}));
		}
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		if (ai instanceof AIRobotSearchBlock) {
			if (!ai.success()) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			} else {
				blockFound = ((AIRobotSearchBlock) ai).blockFound;

				if (!robot.getRegistry().take(new ResourceIdBlock (blockFound), robot)) {
					blockFound = null;
					startDelegateAI(new AIRobotGotoSleep(robot));
				} else {
					startDelegateAI(new AIRobotGotoBlock(robot, ((AIRobotSearchBlock) ai).path));
				}
			}
		} else if (ai instanceof AIRobotGotoBlock) {
			if (!ai.success()) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			} else {
				startDelegateAI(new AIRobotPumpBlock(robot, blockFound));
			}
		} else if (ai instanceof AIRobotGotoStationAndUnloadFluids) {
			robot.getRegistry().take(new ResourceIdBlock (blockFound), robot);

			if (!ai.success()) {
				startDelegateAI(new AIRobotGotoSleep(robot));
			}
		}
	}

	public void updateFilter() {
		fluidFilter.clear();

		DockingStation station = (DockingStation) robot.getLinkedStation();

		for (StatementSlot slot : new ActionIterator(station.getPipe().pipe)) {
			if (slot.statement instanceof ActionRobotFilter) {
				for (IStatementParameter p : slot.parameters) {
					if (p != null && p instanceof StatementParameterItemStack) {
						StatementParameterItemStack param = (StatementParameterItemStack) p;
						ItemStack stack = param.getItemStack();

						if (stack != null) {
							FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);

							if (fluid != null) {
								fluidFilter.add(fluid.getFluid());
							}
						}
					}
				}
			}
		}
	}

	private boolean matchesGateFilter(World world, int x, int y, int z) {
		if (fluidFilter.size() == 0) {
			return true;
		}

        Block block;
		synchronized (world) {
			block = world.getBlock(x, y, z);
		}

        Fluid fluid = FluidRegistry.lookupFluidForBlock(block);

        for (Fluid f : fluidFilter) {
            if (f.getID() == fluid.getID()) {
                return true;
            }
        }

        return false;
	}

}
