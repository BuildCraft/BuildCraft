/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import net.minecraft.world.World;

import buildcraft.api.enums.EnumFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.Box;
import buildcraft.core.blueprints.Template;

public final class PatternFill extends FillerPattern {

    public static final PatternFill INSTANCE = new PatternFill();

    private PatternFill() {
        super("fill", EnumFillerPattern.FILL);
    }

    @Override
    public Template getTemplate(Box box, World world, IStatementParameter[] parameters) {
        Template bpt = new Template(box.size());

        fill(0, 0, 0, box.size().getX() - 1, box.size().getY() - 1, box.size().getZ() - 1, bpt);

        return bpt;
    }
}
