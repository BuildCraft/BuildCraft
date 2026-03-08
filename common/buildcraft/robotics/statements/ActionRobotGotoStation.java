/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.statements;

import buildcraft.api.core.render.ISprite;
import buildcraft.api.items.IMapLocation;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IRobotRegistry;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementParameterItemStack;
import buildcraft.core.statements.BCStatement;
import buildcraft.robotics.BCRoboticsSprites;
import buildcraft.robotics.RobotUtils;
import buildcraft.robotics.ai.AIRobotGoAndLinkToDock;
import buildcraft.robotics.entity.EntityRobot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.List;

public class ActionRobotGotoStation extends BCStatement implements IActionInternal {

    public ActionRobotGotoStation() {
        super("buildcraft:robot.goto_station");
    }

    @Override
    public ITextComponent getDescription() {
        return new TranslationTextComponent("gate.action.robot.goto_station");
    }

    @Override
    public String getDescriptionKey() {
        return "gate.action.robot.goto_station";
    }

    @Nullable
    @Override
    public ISprite getSprite() {
        return BCRoboticsSprites.ACTION_ROBOT_GOTO_STATION;
    }

    @Override
    public void actionActivate(IStatementContainer container, IStatementParameter[] parameters) {
        IRobotRegistry registry = RobotManager.registryProvider.getRegistry(container.getTile().getLevel());

        List<DockingStation> stations = RobotUtils.getStations(container.getTile());

        for (DockingStation station : stations) {
            if (station.robotTaking() != null) {
                EntityRobot robot = (EntityRobot) station.robotTaking();
                AIRobot ai = robot.getOverridingAI();

                if (ai != null) {
                    continue;
                }

                DockingStation newStation = station;

                if (parameters[0] != null) {
                    newStation = getStation((StatementParameterItemStack) parameters[0], registry);
                }

                if (newStation != null) {
                    robot.overrideAI(new AIRobotGoAndLinkToDock(robot, newStation));
                }
            }
        }
    }

    private DockingStation getStation(StatementParameterItemStack stackParam, IRobotRegistry registry) {
        ItemStack item = stackParam.getItemStack();

        if (item != null && item.getItem() instanceof IMapLocation) {
            IMapLocation map = (IMapLocation) item.getItem();
            BlockPos index = map.getPoint(item);

            if (index != null) {
                Direction side = map.getPointSide(item);
                DockingStation paramStation = registry.getStation(index, side);

                if (paramStation != null) {
                    return paramStation;
                }
            }
        }
        return null;
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }

}
