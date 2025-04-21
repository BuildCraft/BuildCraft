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

public class TriggerRobotInStation extends BCStatement implements ITriggerInternal {

    public TriggerRobotInStation() {
        super("buildcraft:robot.in.station");
    }

    @Override
    public ITextComponent getDescription() {
        return new TranslationTextComponent("gate.trigger.robot.in.station");
    }

    @Override
    public String getDescriptionKey() {
        return "gate.trigger.robot.in.station";
    }

    @Nullable
    @Override
    public ISprite getSprite() {
        return BCRoboticsSprites.ACTION_ROBOT_IN_STATION;
    }

    @Override
    public int minParameters() {
        return 0;
    }

    @Override
    public int maxParameters() {
        // return 1;
        // TODO: Discuss whether we actually want to allow parameters here.
        return 0;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterRobot();
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        List<DockingStation> stations = RobotUtils.getStations(container.getTile());

        for (DockingStation station : stations) {
            if (station.robotTaking() != null) {
                EntityRobot robot = (EntityRobot) station.robotTaking();

                if (robot.getDockingStation() == station) {
                    if (parameters.length > 0 && parameters[0] != null && parameters[0].getItemStack() != null) {
                        if (StatementParameterRobot.matches(parameters[0], robot)) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
