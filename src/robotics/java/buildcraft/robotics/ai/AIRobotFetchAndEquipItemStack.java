/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.inventory.ITransactor;
import buildcraft.core.lib.inventory.Transactor;
import buildcraft.core.lib.inventory.filters.AggregateFilter;
import buildcraft.core.lib.inventory.filters.IStackFilter;
import buildcraft.robotics.statements.ActionRobotFilterTool;

public class AIRobotFetchAndEquipItemStack extends AIRobot {

    private IStackFilter filter;
    private int delay = 0;

    public AIRobotFetchAndEquipItemStack(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotFetchAndEquipItemStack(EntityRobotBase iRobot, IStackFilter iFilter) {
        this(iRobot);

        filter = new AggregateFilter(ActionRobotFilterTool.getGateFilter(iRobot.getLinkedStation()), iFilter);
    }

    @Override
    public void update() {
        if (robot.getDockingStation() == null) {
            startDelegateAI(new AIRobotGotoStationToLoad(robot, filter, 1));
        } else {
            if (delay++ > 40) {
                if (equipItemStack()) {
                    terminate();
                } else {
                    delay = 0;
                    startDelegateAI(new AIRobotGotoStationToLoad(robot, filter, 1));
                }
            }
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationToLoad) {
            if (filter == null) {
                // filter can't be retreived, usually because of a load operation.
                // Force a hard abort, preventing parent AI to continue normal
                // sequence of actions and possibly re-starting this.
                abort();
                return;
            }
            if (!ai.success()) {
                setSuccess(false);
                terminate();
            }
        }
    }

    private boolean equipItemStack() {
        if (robot.getDockingStation() != null) {
            DockingStation station = robot.getDockingStation();

            ItemStack itemFound = null;

            for (EnumFacing dir : EnumFacing.VALUES) {
                TileEntity nearbyTile = robot.worldObj.getTileEntity(station.getPos().offset(dir));

                if (nearbyTile != null && nearbyTile instanceof IInventory) {
                    ITransactor trans = Transactor.getTransactorFor(nearbyTile);

                    itemFound = trans.remove(filter, dir.getOpposite(), true);

                    if (itemFound != null) {
                        break;
                    }
                }
            }

            if (itemFound != null) {
                robot.setItemInUse(itemFound);
                return true;
            }
        }
        return false;
    }
}
