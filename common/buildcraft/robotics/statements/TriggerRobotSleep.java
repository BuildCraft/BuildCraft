/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.core.statements.BCStatement;
import buildcraft.robotics.BCRoboticsSprites;
import buildcraft.robotics.RobotUtils;
import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.List;

public class TriggerRobotSleep extends BCStatement implements ITriggerInternal {

    public TriggerRobotSleep() {
        super("buildcraft:robot.sleep");
    }

    @Override
    public ITextComponent getDescription() {
        return new TranslationTextComponent("gate.trigger.robot.sleep");
    }

    @Override
    public String getDescriptionKey() {
        return "gate.trigger.robot.sleep";
    }

    @Nullable
    @Override
    public ISprite getSprite() {
        return BCRoboticsSprites.TRIGGER_ROBOT_SLEEP;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        List<DockingStation> stations = RobotUtils.getStations(container.getTile());

        for (DockingStation station : stations) {
            if (station.robotTaking() != null) {
                EntityRobot robot = (EntityRobot) station.robotTaking();

                if (robot.isActive()) {
                    return true;
                }
            }
        }

        return false;
    }
}
