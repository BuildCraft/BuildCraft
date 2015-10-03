/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.statements;

import net.minecraft.item.ItemStack;

import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.api.statements.StatementSlot;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.core.statements.BCStatement;
import buildcraft.robotics.item.ItemRobot;

public class ActionStationForbidRobot extends BCStatement implements IActionInternal {
    private final boolean invert;

    public ActionStationForbidRobot(boolean invert) {
        super("buildcraft:station." + (invert ? "force" : "forbid") + "_robot");
        setLocation("buildcraftrobotics:triggers/action_station_robot_" + (invert ? "mandatory" : "forbidden"));
        this.invert = invert;
    }

    @Override
    public String getDescription() {
        return StringUtils.localize("gate.action.station." + (invert ? "force" : "forbid") + "_robot");
    }

    // @Override
    // public void registerIcons(TextureAtlasSpriteRegister iconRegister) {
    // icon = iconRegister.registerIcon("buildcraftrobotics:triggers/action_station_robot_" + (invert ? "mandatory" :
    // "forbidden"));
    // }

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
        return new StatementParameterItemStack();
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
            if (p != null) {
                ItemStack stack = p.getItemStack();

                if (stack != null && stack.getItem() instanceof ItemRobot) {
                    if (ItemRobot.getRobotNBT(stack) == robot.getBoard().getNBTHandler()) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {

    }
}
