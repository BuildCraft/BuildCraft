/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IBox;
import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraft.lib.misc.VecUtil;

import buildcraft.core.BCCoreConfig;
import buildcraft.core.BCCoreSprites;
import buildcraft.core.patterns.Pattern;

public class PatternHorizon extends Pattern {

    public PatternHorizon() {
        super("horizon");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getSprite() {
        return BCCoreSprites.FILLER_HORIZON;
    }

    @Override
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        IBox box = filler.getBox();
        BlockPos min = box.min().down();
        if (filler.getFillerWorld().isOutsideBuildHeight(min)) {
            min = box.min();
        }

        int y = Math.min(min.getY() + 256/* BCCoreConfig.maxMachineReachDistance*/, filler.getFillerWorld().getHeight());

        FilledTemplate bpt = new FilledTemplate(min, VecUtil.replaceValue(box.max(), Axis.Y, y));

        if (box.size().getY() > 0) {
            bpt.fillPlaneXZ(0);
        }

        return bpt;
    }
}
