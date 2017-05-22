/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IBox;
import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import buildcraft.core.BCCoreSprites;

public class PatternHorizon extends Pattern {

    public PatternHorizon() {
        super("horizon");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getSpriteHolder() {
        return BCCoreSprites.FILLER_HORIZON;
    }

    @Override
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        IBox box = filler.getBox();
        int minX = box.min().getX();
        int minY = box.min().getY() > 0 ? box.min().getY() - 1 : 0;
        int minZ = box.min().getZ();

        int maxX = box.max().getX();
        int maxY = filler.getTile().getWorld().getHeight();
        int maxZ = box.max().getZ();

        FilledTemplate bpt = new FilledTemplate(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));

        if (box.size().getY() > 0) {
            bpt.fillPlaneXZ(0);
        }

        return bpt;
    }
}
