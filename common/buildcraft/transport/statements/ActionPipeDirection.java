/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import java.util.Locale;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import buildcraft.BuildCraftTransport;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.statements.BCStatement;

public class ActionPipeDirection extends BCStatement implements IActionInternal {

    public final EnumFacing direction;

    public ActionPipeDirection(EnumFacing direction) {
        super("buildcraft:pipe.dir." + direction.name().toLowerCase(Locale.ROOT), "buildcraft.pipe.dir." + direction.name().toLowerCase(Locale.ROOT));
        setBuildCraftLocation("core", "triggers/trigger_dir_" + direction.name().toLowerCase(Locale.ROOT));
        this.direction = direction;
    }

    @Override
    public String getDescription() {
        return direction.name().substring(0, 1) + direction.name().substring(1).toLowerCase(Locale.ROOT) + " Pipe Direction";
    }

    @Override
    public IStatement rotateLeft() {
        EnumFacing face = direction.getAxis() == Axis.Y ? direction : direction.rotateY();
        return BuildCraftTransport.actionPipeDirection[face.ordinal()];
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}
}
