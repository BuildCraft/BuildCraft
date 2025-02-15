/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.snapshot.pattern;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPatternShape;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.builders.BCBuildersSprites;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PatternFrame extends Pattern implements IFillerPatternShape {
    public PatternFrame() {
        super("frame");
    }

    @Override
    public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
        int maxX = filledTemplate.getMax().getX();
        int maxY = filledTemplate.getMax().getY();
        int maxZ = filledTemplate.getMax().getZ();

        filledTemplate.setLineX(0, maxX, 0, 0, true);
        filledTemplate.setLineX(0, maxX, maxY, 0, true);
        filledTemplate.setLineX(0, maxX, maxY, maxZ, true);
        filledTemplate.setLineX(0, maxX, 0, maxZ, true);

        filledTemplate.setLineY(0, 0, maxY, 0, true);
        filledTemplate.setLineY(maxX, 0, maxY, 0, true);
        filledTemplate.setLineY(maxX, 0, maxY, maxZ, true);
        filledTemplate.setLineY(0, 0, maxY, maxZ, true);

        filledTemplate.setLineZ(0, 0, 0, maxZ, true);
        filledTemplate.setLineZ(maxX, 0, 0, maxZ, true);
        filledTemplate.setLineZ(maxX, maxY, 0, maxZ, true);
        filledTemplate.setLineZ(0, maxY, 0, maxZ, true);

        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public SpriteHolder getSprite() {
        return BCBuildersSprites.FILLER_FRAME;
    }
}
