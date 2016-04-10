/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.enums.EnumFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.Box;
import buildcraft.core.blueprints.BptBuilderTemplate;
import buildcraft.core.blueprints.Template;

public class PatternFlatten extends FillerPattern {

    public PatternFlatten() {
        super("flatten", EnumFillerPattern.FLATTEN);
    }

    @Override
    public Template getTemplate(Box box, World world, IStatementParameter[] parameters) {
        int xMin = box.min().getX();
        int yMin = box.min().getY() > 0 ? (int) box.min().getY() - 1 : 0;
        int zMin = box.min().getZ();

        int xMax = box.max().getX();
        int yMax = box.max().getY();
        int zMax = box.max().getZ();

        Template bpt = new Template(new BlockPos(box.size().getX(), yMax - yMin + 1, box.size().getZ()));

        if (box.min().getY() > 0) {
            for (int x = xMin; x <= xMax; ++x) {
                for (int z = zMin; z <= zMax; ++z) {
                    bpt.set(new BlockPos(x - xMin, 0, z - zMin), new SchematicMask(true));
                }
            }
        }

        return bpt;
    }

    @Override
    public BptBuilderTemplate getTemplateBuilder(Box box, World world, IStatementParameter[] parameters) {
        int yMin = box.min().getY() > 0 ? (int) box.min().getY() - 1 : 0;

        return new BptBuilderTemplate(getTemplate(box, world, parameters), world, new BlockPos(box.min().getX(), yMin, box.min().getZ()));
    }
}
