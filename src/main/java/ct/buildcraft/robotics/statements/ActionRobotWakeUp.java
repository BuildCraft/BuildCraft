package ct.buildcraft.robotics.statements;

import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.statements.IActionInternal;
import ct.buildcraft.api.statements.IStatementContainer;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.core.statements.BCStatement;
import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.robotics.BCRoboticsSprites;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ActionRobotWakeUp extends BCStatement implements IActionInternal {

    public ActionRobotWakeUp() {
        super("buildcraft:robot.wakeup");
    }

    @Override
    public Component getDescription() {
        return Component.translatable("gate.action.robot.wakeUp");
    }

    @Override
    public void actionActivate(IStatementContainer container, IStatementParameter[] parameters) {
        DockingStation station = RobotStatementUtils.getStation(container);
        if (station == null) return;
        EntityRobotBase robot = station.robotTaking();
        if (robot == null) return;
        // Clear sleep override so robot resumes normal AI
        robot.setMainAIOverride(null);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public ISprite getSprite() {
        return BCRoboticsSprites.ACTION_ROBOT_WAKEUP;
    }
}
