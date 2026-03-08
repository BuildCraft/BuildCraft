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
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

public class AIRobotGotoStation extends AIRobot {

    private BlockPos stationIndex;
    private Direction stationSide;

    public AIRobotGotoStation(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotGotoStation(EntityRobotBase iRobot, DockingStation station) {
        this(iRobot);

        stationIndex = station.index();
        stationSide = station.side();
        setSuccess(false);
    }

    @Override
    public void start() {
        DockingStation station = robot.getRegistry().getStation(stationIndex, stationSide);

        if (station == null) {
            terminate();
        } else if (station == robot.getDockingStation()) {
            setSuccess(true);
            terminate();
        } else {
            if (station.take(robot)) {
                startDelegateAI(new AIRobotGotoBlock(robot, station.getPos().relative(stationSide)));
            } else {
                terminate();
            }
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        DockingStation station = robot.getRegistry().getStation(stationIndex, stationSide);

        if (station == null) {
            terminate();
        } else if (ai instanceof AIRobotGotoBlock) {
            if (ai.success()) {
                startDelegateAI(new AIRobotStraightMoveTo(robot, VecUtil.convertCenter(stationIndex).add(VecUtil.convert(stationSide, 0.5))));
            } else {
                terminate();
            }
        } else {
            setSuccess(true);
            if (stationSide.getAxis() != Axis.Y) {
                robot.aimItemAt(stationIndex.relative(stationSide, 2));
            } else {
                robot.aimItemAt(Mth.floor(robot.getAimYaw() / 90f) * 90f + 180f, robot.getAimPitch());
            }
            robot.dock(station);
            terminate();
        }
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        nbt.put("stationIndex", NBTUtilBC.writeBlockPos(stationIndex));
        nbt.putByte("stationSide", (byte) stationSide.ordinal());
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        stationIndex = NBTUtilBC.readBlockPos(nbt.get("stationIndex"));
        stationSide = Direction.values()[nbt.getByte("stationSide")];
    }
}
