/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementSlot;
import buildcraft.core.statements.BCStatement;
import buildcraft.robotics.BCRoboticsSprites;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.Nullable;

public class ActionStationForbidRobot extends BCStatement implements IActionInternal {
    private final boolean invert;

    public ActionStationForbidRobot(boolean invert) {
        super("buildcraft:station." + (invert ? "force" : "forbid") + "_robot");
        this.invert = invert;
    }

    @Override
    public Component getDescription() {
        return new TranslatableComponent("gate.action.station." + (invert ? "force" : "forbid") + "_robot");
    }

    @Override
    public String getDescriptionKey() {
        return "gate.action.station." + (invert ? "force" : "forbid") + "_robot";
    }

    @Nullable
    @Override
    public ISprite getSprite() {
        return invert ? BCRoboticsSprites.ACTION_STATION_ROBOT_MANDATORY : BCRoboticsSprites.ACTION_STATION_ROBOT_FORBIDDEN;
    }

    @Override
    public int minParameters() {
        return 1;
    }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterRobot();
    }

    public static boolean isForbidden(DockingStation station, EntityRobotBase robot) {
        for (StatementSlot s : station.getActiveActions()) {
            if (s.statement instanceof ActionStationForbidRobot) {
                if (((ActionStationForbidRobot) s.statement).invert ^ ActionStationForbidRobot.isForbidden(s, robot)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isForbidden(StatementSlot slot, EntityRobotBase robot) {
        for (IStatementParameter p : slot.parameters) {
            if (p != null && StatementParameterRobot.matches(p, robot)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}
}
