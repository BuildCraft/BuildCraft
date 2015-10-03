/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import net.minecraft.world.World;

import buildcraft.api.enums.EnumFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.Box;
import buildcraft.core.blueprints.Template;

public class PatternClear extends FillerPattern {

    public PatternClear() {
        super("clear", EnumFillerPattern.CLEAR);
    }

    @Override
    public Template getTemplate(Box box, World world, IStatementParameter[] parameters) {
        int xMin = (int) box.pMin().xCoord;
        int yMin = (int) box.pMin().yCoord;
        int zMin = (int) box.pMin().zCoord;

        int xMax = (int) box.pMax().xCoord;
        int yMax = (int) box.pMax().yCoord;
        int zMax = (int) box.pMax().zCoord;

        Template bpt = new Template(xMax - xMin + 1, yMax - yMin + 1, zMax - zMin + 1);

        return bpt;
    }
}
