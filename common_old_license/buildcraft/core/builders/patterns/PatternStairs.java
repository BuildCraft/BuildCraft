/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.patterns;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import buildcraft.core.BCCoreSprites;

public class PatternStairs extends Pattern {

    public PatternStairs() {
        super("stairs");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getSprite() {
        return BCCoreSprites.FILLER_STAIRS;
    }

    @Override
    public int maxParameters() {
        return 2;
    }

    @Override
    public int minParameters() {
        return 2;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return index == 1 ? PatternParameterXZDir.EAST : PatternParameterYDir.UP;
    }

    @Override
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        FilledTemplate template = new FilledTemplate(filler.getBox());

        PatternParameterXZDir xzDir = getParam(0, params, PatternParameterXZDir.EAST);
        PatternParameterYDir yDir = getParam(1, params, PatternParameterYDir.UP);

        int y;
        final int yStep;
        final int yEnd;
        if (yDir == PatternParameterYDir.UP) {
            y = 0;
            yStep = 1;
            yEnd = template.maxY;
        } else {
            y = template.maxY;
            yStep = -1;
            yEnd = 0;
        }

        final int dfx = xzDir.dir.getFrontOffsetX() > 0 ? 1 : 0;
        final int dfz = xzDir.dir.getFrontOffsetZ() > 0 ? 1 : 0;
        final int dtx = xzDir.dir.getFrontOffsetX() < 0 ? -1 : 0;
        final int dtz = xzDir.dir.getFrontOffsetZ() < 0 ? -1 : 0;

        int fx = 0;
        int fz = 0;
        int tx = template.maxX;
        int tz = template.maxZ;

        while (y != yEnd) {
            template.fillAreaXZ(fx, tx, y, fz, tz);

            fx += dfx;
            fz += dfz;
            tx += dtx;
            tz += dtz;
            y += yStep;

            if (fx > tx) break;
            if (fz > tz) break;
        }

        return template;
    }
}
