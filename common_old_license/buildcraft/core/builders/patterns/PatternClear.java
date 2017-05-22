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

public class PatternClear extends Pattern {
    public PatternClear() {
        super("clear");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getGuiSprite() {
        return BCCoreSprites.FILLER_CLEAR;
    }

    @Override
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        return new FilledTemplate(filler.getBox());
    }
}
