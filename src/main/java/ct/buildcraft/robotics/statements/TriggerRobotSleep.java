package ct.buildcraft.robotics.statements;

import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.ITriggerInternal;
import ct.buildcraft.core.statements.BCStatement;
import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.robotics.BCRoboticsSprites;
import ct.buildcraft.robotics.entity.EntityRobot;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TriggerRobotSleep extends BCStatement implements ITriggerInternal {

    public TriggerRobotSleep() {
        super("buildcraft:robot.sleep");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.trigger.robot.sleep");
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        DockingStation station = RobotStatementUtils.getStation(container);
        if (station == null) return false;
        EntityRobotBase robot = station.robotTaking();
        if (robot == null || robot.getDockingStation() != station) return false;
        if (robot instanceof EntityRobot er) {
            return er.isAsleepForRendering();
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISprite getSprite() {
        return BCRoboticsSprites.TRIGGER_ROBOT_SLEEP;
    }
}
