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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.List;

public class TriggerRobotLinked extends BCStatement implements ITriggerInternal {
    private final boolean reserved;

    public TriggerRobotLinked(boolean reserved) {
        super("buildcraft:robot." + (reserved ? "reserved" : "linked"));
        this.reserved = reserved;
    }

    @Override
    public ITextComponent getDescription() {
        return new TranslationTextComponent("gate.trigger.robot." + (reserved ? "reserved" : "linked"));
    }

    @Override
    public String getDescriptionKey() {
        return "gate.trigger.robot." + (reserved ? "reserved" : "linked");
    }

    @Nullable
    @Override
    public ISprite getSprite() {
        return reserved ? BCRoboticsSprites.ACTION_ROBOT_RESERVED : BCRoboticsSprites.ACTION_ROBOT_LINKED;
    }

    @Override
    public boolean isTriggerActive(IStatementContainer container, IStatementParameter[] parameters) {
        List<DockingStation> stations = RobotUtils.getStations(container.getTile());

        for (DockingStation station : stations) {
            if (station.isTaken() && (reserved || station.isMainStation())) {
                return true;
            }
        }

        return false;
    }
}
