/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.World;

import buildcraft.api.blueprints.SchematicMask;
import buildcraft.api.enums.EnumFillerPattern;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.core.Box;
import buildcraft.core.blueprints.BptBuilderTemplate;
import buildcraft.core.blueprints.Template;
import buildcraft.core.lib.utils.Utils;

public class PatternHorizon extends FillerPattern {

    public PatternHorizon() {
        super("horizon", EnumFillerPattern.HORIZON);
    }

    @Override
    public Template getTemplate(Box box, World world, IStatementParameter[] parameters) {
        int xMin = (int) box.min().getX();
        int yMin = box.min().getY() > 0 ? (int) box.min().getY() - 1 : 0;
        int zMin = (int) box.min().getZ();

        int xMax = (int) box.max().getX();
        int yMax = world.getActualHeight();
        int zMax = (int) box.max().getZ();

        Template bpt = new Template(Utils.withValue(box.size(), Axis.Y, yMax - yMin));

        if (box.size().getY() > 0) {
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
