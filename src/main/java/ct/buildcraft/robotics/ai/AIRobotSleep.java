package ct.buildcraft.robotics.ai;

import ct.buildcraft.api.robots.AIRobot;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.statements.StatementSlot;
import ct.buildcraft.robotics.statements.ActionRobotWakeUp;
import net.minecraft.nbt.CompoundTag;

public class AIRobotSleep extends AIRobot {
    private static final int SLEEPING_TIME = 60 * 20;
    private int sleptTime;

    public AIRobotSleep(EntityRobotBase robot) {
        super(robot);
    }

    @Override
    public void preempt(AIRobot ai) {
        DockingStation station = robot.getLinkedStation();
        if (station == null) {
            return;
        }

        for (StatementSlot slot : station.getActiveActions()) {
            if (slot.statement instanceof ActionRobotWakeUp) {
                terminate();
                return;
            }
        }
    }

    @Override
    public void update() {
        sleptTime++;
        if (sleptTime > SLEEPING_TIME) {
            terminate();
        }
    }

    @Override
    public int getEnergyCost() {
        return sleptTime % 10 == 0 ? 1 : 0;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        nbt.putInt("sleptTime", sleptTime);
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        sleptTime = nbt.getInt("sleptTime");
    }
}
