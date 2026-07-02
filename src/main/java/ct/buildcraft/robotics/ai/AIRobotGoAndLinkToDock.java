package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.core.BlockIndex;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

/**
 * Gate-command AI from BuildCraft 7.1.x: move the robot to the requested station and make that station its main dock.
 * This is intentionally different from AIRobotGotoStation, which only reserves a temporary station for charging/unload.
 */
public class AIRobotGoAndLinkToDock extends AIRobot {
    private BlockIndex stationIndex;
    private Direction stationSide;

    public AIRobotGoAndLinkToDock(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotGoAndLinkToDock(EntityRobotBase robot, DockingStation station) {
        this(robot);
        if (station != null) {
            stationIndex = station.index();
            stationSide = station.side();
        }
    }

    @Override
    public void start() {
        DockingStation station = getStation();
        if (station == null) {
            setSuccess(false);
            terminate();
            return;
        }
        if (station == robot.getLinkedStation() && station == robot.getDockingStation()) {
            terminate();
            return;
        }
        if (!station.takeAsMain(robot)) {
            setSuccess(false);
            terminate();
            return;
        }

        Direction side = station.side();
        int dx = side == null ? 0 : side.getStepX();
        int dy = side == null ? 0 : side.getStepY();
        int dz = side == null ? 0 : side.getStepZ();
        int targetX = station.x() + dx * 2;
        int targetY = station.y() + dy * 2;
        int targetZ = station.z() + dz * 2;
        startDelegateAI(new AIRobotGotoBlock(robot, targetX, targetY, targetZ, pathRangeTo(targetX, targetY, targetZ)));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        DockingStation station = getStation();
        if (station == null) {
            setSuccess(false);
            terminate();
            return;
        }
        if (ai instanceof AIRobotGotoBlock) {
            if (ai.success()) {
                Direction side = station.side();
                int dx = side == null ? 0 : side.getStepX();
                int dy = side == null ? 0 : side.getStepY();
                int dz = side == null ? 0 : side.getStepZ();
                startDelegateAI(new AIRobotStraightMoveTo(robot,
                        station.x() + 0.5D + dx * 0.5D,
                        station.y() + 0.5D + dy * 0.5D,
                        station.z() + 0.5D + dz * 0.5D));
            } else {
                setSuccess(false);
                terminate();
            }
        } else if (ai instanceof AIRobotStraightMoveTo) {
            if (ai.success()) {
                Direction side = station.side();
                if (side != null && side.getStepY() == 0) {
                    robot.aimItemAt(station.x() + 2 * side.getStepX(), station.y(), station.z() + 2 * side.getStepZ());
                } else {
                    robot.aimItemAt(Mth.floor(robot.getAimYaw() / 90.0F) * 90.0F + 180.0F, robot.getAimPitch());
                }
                robot.dock(station);
            } else {
                setSuccess(false);
            }
            terminate();
        }
    }

    private double pathRangeTo(int x, int y, int z) {
        double dx = robot.getX() - (x + 0.5D);
        double dy = robot.getY() - (y + 0.5D);
        double dz = robot.getZ() - (z + 0.5D);
        return Math.max(32.0D, Math.sqrt(dx * dx + dy * dy + dz * dz) + 16.0D);
    }

    private DockingStation getStation() {
        if (stationIndex == null || robot.getRegistry() == null) {
            return robot.getLinkedStation();
        }
        return robot.getRegistry().getStation(stationIndex.toBlockPos(), stationSide);
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        if (stationIndex != null) {
            CompoundTag idx = new CompoundTag();
            stationIndex.writeTo(idx);
            nbt.put("stationIndex", idx);
            nbt.putByte("stationSide", (byte) (stationSide == null ? -1 : stationSide.ordinal()));
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        if (nbt.contains("stationIndex")) {
            stationIndex = new BlockIndex(nbt.getCompound("stationIndex"));
            int side = nbt.getByte("stationSide");
            stationSide = side >= 0 && side < Direction.values().length ? Direction.values()[side] : null;
        } else if (robot.getLinkedStation() != null) {
            stationIndex = robot.getLinkedStation().index();
            stationSide = robot.getLinkedStation().side();
        }
    }
}
