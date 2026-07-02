package ct.buildcraft.robotics.statements;

import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.ITriggerInternal;
import ct.buildcraft.core.statements.BCStatement;
import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.robotics.BCRoboticsSprites;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TriggerRobotInStation extends BCStatement implements ITriggerInternal {

    public TriggerRobotInStation() {
        super("buildcraft:robot.in.station");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.trigger.robot.inStation");
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        DockingStation station = RobotStatementUtils.getStation(container);
        if (station == null) return false;
        EntityRobotBase robot = station.robotTaking();
        return robot != null && robot.getDockingStation() == station;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISprite getSprite() {
        return BCRoboticsSprites.TRIGGER_ROBOT_IN_STATION;
    }
}
