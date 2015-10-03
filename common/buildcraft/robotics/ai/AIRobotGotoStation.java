/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.ai;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.MathHelper;

import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.lib.utils.NBTUtils;
import buildcraft.core.lib.utils.Utils;

public class AIRobotGotoStation extends AIRobot {

    private BlockPos stationIndex;
    private EnumFacing stationSide;

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
                startDelegateAI(new AIRobotGotoBlock(robot, station.getPos().offset(stationSide)));
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
            startDelegateAI(new AIRobotStraightMoveTo(robot, Utils.convertMiddle(stationIndex).add(Utils.convert(stationSide, 0.5))));
        } else {
            setSuccess(true);
            if (stationSide.getAxis() != Axis.Y) {
                robot.aimItemAt(stationIndex.offset(stationSide, 2));
            } else {
                robot.aimItemAt(MathHelper.floor_float(robot.getAimYaw() / 90f) * 90f + 180f, robot.getAimPitch());
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
    public void writeSelfToNBT(NBTTagCompound nbt) {
        nbt.setTag("stationIndex", NBTUtils.writeBlockPos(stationIndex));
        nbt.setByte("stationSide", (byte) stationSide.ordinal());
    }

    @Override
    public void loadSelfFromNBT(NBTTagCompound nbt) {
        stationIndex = NBTUtils.readBlockPos(nbt.getTag("stationIndex"));
        stationSide = EnumFacing.values()[nbt.getByte("stationSide")];
    }
}
