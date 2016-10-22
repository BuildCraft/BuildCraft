/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.statements;

import java.util.Locale;

import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.StatCollector;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.core.statements.BCStatement;
import buildcraft.lib.misc.StringUtilBC;

public class ActionPipeColor extends BCStatement implements IActionInternal {

    public final EnumDyeColor color;

    public ActionPipeColor(EnumDyeColor color) {
        super("buildcraft:pipe.color." + color.getName(), "buildcraft.pipe." + color.getName());
        setBuildCraftLocation("core", "items/paintbrush/" + color.name().toLowerCase(Locale.ROOT));
        this.color = color;
    }

    @Override
    public String getDescription() {
        return String.format(StringUtilBC.localize("gate.action.pipe.item.color"), StatCollector.translateToLocal(color.getUnlocalizedName()));
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {

    }
}
