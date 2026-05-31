/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the MinecraftClient Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.snapshot.pattern;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.filler.IFillerPatternShape;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import buildcraft.builders.BCBuildersSprites;

public class PatternFill extends Pattern implements IFillerPatternShape {
    public PatternFill() {
        super("fill");
    }

    @Override
    @Environment(EnvType.CLIENT)
    public SpriteHolder getSprite() {
        return BCBuildersSprites.FILLER_FILL;
    }

    @Override
    public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
        filledTemplate.setAll(true);
        return true;
    }
}
