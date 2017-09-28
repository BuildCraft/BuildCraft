/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IBox;
import buildcraft.api.core.render.ISprite;
import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.core.BCCoreSprites;
import buildcraft.core.patterns.Pattern;

public class PatternFlatten extends Pattern {

    public PatternFlatten() {
        super("flatten");
    }

    @Override
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        IBox box = filler.getBox();

        World world = filler.getFillerWorld();

        final int maxReachDist = 256;// TODO: Add a config for this!

        int sx = box.size().getX();
        int sz = box.size().getZ();
        int[][] depth = new int[sx][sz];
        int maxDepth = 0;

        BlockPos min = box.min();
        int dx = min.getX();
        int dz = min.getZ();

        int yStart = min.getY();

        for (int x = 0; x < sx; x++) {
            int px = x + dx;
            for (int z = 0; z < sz; z++) {
                int pz = z + dz;
                int y = 1;
                for (; y < maxReachDist; y++) {
                    if (!world.isAirBlock(new BlockPos(px, yStart - y, pz))) {
                        break;
                    }
                }
                depth[x][z] = y;
                maxDepth = Math.max(y, maxDepth);
            }
        }

        min = min.down(maxDepth + 1);
        FilledTemplate tpl = new FilledTemplate(min, box.max());

        for (int x = 0; x < sx; x++) {
            for (int z = 0; z < sz; z++) {
                int d = maxDepth - depth[x][z] + 1;
                for (int y = 0; y <= d; y++) {
                    tpl.ignore(x, y, z);
                }
                tpl.fillLineY(x, d + 1, maxDepth, z);
            }
        }

        return tpl;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ISprite getSprite() {
        return BCCoreSprites.FILLER_FLATTEN;
    }
}
