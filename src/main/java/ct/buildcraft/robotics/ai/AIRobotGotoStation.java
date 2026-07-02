package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.core.BlockIndex;
import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public class AIRobotGotoStation extends AIRobot {
    private BlockIndex stationIndex;
    private Direction stationSide;
    private boolean reservedStation;

    public AIRobotGotoStation(EntityRobotBase robot) {
        super(robot);
    }

    public AIRobotGotoStation(EntityRobotBase robot, DockingStation station) {
        this(robot);
        stationIndex = station.index();
        stationSide = station.side();
        setSuccess(false);
    }

    @Override
    public void start() {
        DockingStation station = getStation();
        if (station == null) {
            setSuccess(false);
            terminate();
        } else if (station == robot.getDockingStation()) {
            setSuccess(true);
            terminate();
        } else if (station.take(robot)) {
            reservedStation = true;
            Direction side = station.side();
            int dx = side == null ? 0 : side.getStepX();
            int dy = side == null ? 0 : side.getStepY();
            int dz = side == null ? 0 : side.getStepZ();
            int targetX = station.x() + dx;
            int targetY = station.y() + dy;
            int targetZ = station.z() + dz;
            startDelegateAI(new AIRobotGotoBlock(robot, targetX, targetY, targetZ, pathRangeTo(targetX, targetY, targetZ)));
        } else {
            setSuccess(false);
            terminate();
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        DockingStation station = getStation();
        if (station == null) {
            setSuccess(false);
            terminate();
        } else if (ai instanceof AIRobotGotoBlock) {
            if (ai.success()) {
                Direction side = station.side();
                int dx = side == null ? 0 : side.getStepX();
                int dy = side == null ? 0 : side.getStepY();
                int dz = side == null ? 0 : side.getStepZ();
                startDelegateAI(new AIRobotStraightMoveTo(robot, station.x() + 0.5D + dx * 0.5D,
                        station.y() + 0.5D + dy * 0.5D, station.z() + 0.5D + dz * 0.5D));
            } else {
                releaseReservation(station);
                setSuccess(false);
                terminate();
            }
        } else if (ai instanceof AIRobotStraightMoveTo) {
            if (ai.success()) {
                setSuccess(true);
                robot.setDeltaMovement(Vec3.ZERO);
                if (station.side() != null && station.side().getStepY() == 0) {
                    robot.aimItemAt(station.x() + 2 * station.side().getStepX(), station.y(), station.z() + 2 * station.side().getStepZ());
                }
                robot.dock(station);
            } else {
                releaseReservation(station);
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

    private void releaseReservation(DockingStation station) {
        if (reservedStation && station != null && station != robot.getLinkedStation() && robot.getDockingStation() != station) {
            station.release(robot);
            reservedStation = false;
        }
    }

    private DockingStation getStation() {
        if (stationIndex == null || robot.getRegistry() == null) return null;
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
        }
    }
}
