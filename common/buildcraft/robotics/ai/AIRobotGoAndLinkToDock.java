/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

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
                startDelegateAI(new AIRobotGotoBlock(robot, station.getPos().relative(station.side(), 2)));
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
                startDelegateAI(new AIRobotStraightMoveTo(robot, VecUtil.convertCenter(station.getPos()).add(VecUtil.convert(station.side(), 0.5))));
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
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);

        if (station != null && station.index() != null) {
            nbt.put("stationIndex", NBTUtilBC.writeBlockPos(station.index()));
            nbt.putByte("stationSide", (byte) station.side().ordinal());
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        if (nbt.contains("stationIndex")) {
            BlockPos index = NBTUtilBC.readBlockPos(nbt.get("stationIndex"));
            Direction side = Direction.values()[nbt.getByte("stationSide")];

            station = robot.getRegistry().getStation(index, side);
        } else {
            station = robot.getLinkedStation();
        }
    }
}
