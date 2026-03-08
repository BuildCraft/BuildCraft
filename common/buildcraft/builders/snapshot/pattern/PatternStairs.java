/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.snapshot.pattern;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPatternShape;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterXZDir;
import buildcraft.builders.snapshot.pattern.parameter.PatternParameterYDir;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PatternStairs extends Pattern implements IFillerPatternShape {
    public PatternStairs() {
        super("stairs");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public SpriteHolder getSprite() {
        return BCBuildersSprites.FILLER_STAIRS;
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

    // TODO: convert to for loops?
    @Override
    public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
        PatternParameterYDir yDir = getParam(0, params, PatternParameterYDir.UP);
        PatternParameterXZDir xzDir = getParam(1, params, PatternParameterXZDir.EAST);

        int y = yDir == PatternParameterYDir.UP ? 0 : filledTemplate.getMax().getY();
        final int yStep = yDir == PatternParameterYDir.UP ? 1 : -1;
        final int yEnd = yDir == PatternParameterYDir.UP ? filledTemplate.getMax().getY() + 1 : -1;

        int fx = 0;
        int fz = 0;
        int tx = filledTemplate.getMax().getX();
        int tz = filledTemplate.getMax().getZ();

        while (y != yEnd) {
            filledTemplate.setAreaXZ(fx, tx, y, fz, tz, true);

//            fx += xzDir.dir.getFrontOffsetX() > 0 ? 1 : 0;
            fx += xzDir.dir.getStepX() > 0 ? 1 : 0;
//            fz += xzDir.dir.getFrontOffsetZ() > 0 ? 1 : 0;
            fz += xzDir.dir.getStepZ() > 0 ? 1 : 0;
//            tx += xzDir.dir.getFrontOffsetX() < 0 ? -1 : 0;
            tx += xzDir.dir.getStepX() < 0 ? -1 : 0;
//            tz += xzDir.dir.getFrontOffsetZ() < 0 ? -1 : 0;
            tz += xzDir.dir.getStepZ() < 0 ? -1 : 0;
            y += yStep;

            if (fx > tx) break;
            if (fz > tz) break;
        }
        return true;
    }
}
