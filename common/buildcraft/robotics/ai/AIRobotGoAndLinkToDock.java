/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.robotics.ai.path.AIRobotGotoBlock;

public class AIRobotGoAndLinkToDock extends AIRobot {

    private DockingStation station;

    public AIRobotGoAndLinkToDock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotGoAndLinkToDock(EntityRobotBase iRobot, DockingStation iStation) {
        this(iRobot);

        station = iStation;
    }

    @Override
    public void start() {
        if (station == robot.getLinkedStation() && station == robot.getDockingStation()) {
            terminate();
        } else {
            if (station != null && station.takeAsMain(robot)) {
                startDelegateAI(AIRobotGotoBlock.newSearchAndGotoBlock(robot, station.getPos().offset(station.side(), 2)));
            } else {
                setSuccess(false);
                terminate();
            }
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoBlock) {
            if (ai.success()) {
                startDelegateAI(new AIRobotStraightMoveTo(robot, Utils.convertMiddle(station.getPos()).add(Utils.convert(station.side(), 0.5))));
            } else {
                terminate();
            }
        } else if (ai instanceof AIRobotStraightMoveTo) {
            if (ai.success()) {
                robot.dock(station);
            }
            terminate();
        }
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(NBTTagCompound nbt) {
        super.writeSelfToNBT(nbt);

        if (station != null && station.index() != null) {
            nbt.setTag("stationIndex", NBTUtils.writeBlockPos(station.index()));
            nbt.setByte("stationSide", (byte) station.side().ordinal());
        }
    }

    @Override
    public void loadSelfFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("stationIndex")) {
            BlockPos index = NBTUtils.readBlockPos(nbt.getTag("stationIndex"));
            EnumFacing side = EnumFacing.values()[nbt.getByte("stationSide")];

            station = robot.getRegistry().getStation(index, side);
        } else {
            station = robot.getLinkedStation();
        }
    }
}
