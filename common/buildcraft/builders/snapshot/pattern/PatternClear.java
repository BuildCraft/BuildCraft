/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.snapshot.pattern;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPatternShape;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import buildcraft.core.BCCoreSprites;

public class PatternClear extends Pattern implements IFillerPatternShape {
    public PatternClear() {
        super("clear");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getSprite() {
        return BCCoreSprites.FILLER_CLEAR;
    }

    @Override
    public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
        return true;
    }
}
