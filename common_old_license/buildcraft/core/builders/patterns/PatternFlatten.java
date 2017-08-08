/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IBox;
import buildcraft.api.core.render.ISprite;
import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.core.BCCoreSprites;

public class PatternFlatten extends Pattern {

    public PatternFlatten() {
        super("flatten");
    }

    @Override
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        IBox box = filler.getBox();
        BlockPos min = box.min().down();
        if (filler.getFillerWorld().isOutsideBuildHeight(min)) {
            min = box.min();
        }
        FilledTemplate bpt = new FilledTemplate(min, box.max());

        if (box.size().getY() > 0) {
            bpt.fillPlaneXZ(0);
        }

        return bpt;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ISprite getSprite() {
        return BCCoreSprites.FILLER_FLATTEN;
    }
}
