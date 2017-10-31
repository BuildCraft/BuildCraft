/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.patterns;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.filler.IFilledTemplate;
import buildcraft.api.statements.IStatementParameter;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import buildcraft.core.BCCoreSprites;

public class PatternBox extends Pattern {
    public PatternBox() {
        super("box");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getSprite() {
        return BCCoreSprites.FILLER_BOX;
    }

    @Override
    public boolean fillTemplate(IFilledTemplate filledTemplate, IStatementParameter[] params) {
        filledTemplate.setAreaYZ(
            0,
            0,
            filledTemplate.getMax().getY(),
            0,
            filledTemplate.getMax().getZ(),
            true
        );
        filledTemplate.setAreaYZ(
            filledTemplate.getMax().getX(),
            0,
            filledTemplate.getMax().getY(),
            0,
            filledTemplate.getMax().getZ(),
            true
        );
        filledTemplate.setAreaXZ(
            0,
            filledTemplate.getMax().getX(),
            0,
            0,
            filledTemplate.getMax().getZ(),
            true
        );
        filledTemplate.setAreaXZ(
            0,
            filledTemplate.getMax().getX(),
            filledTemplate.getMax().getY(),
            0,
            filledTemplate.getMax().getZ(),
            true
        );
        filledTemplate.setAreaXY(
            0,
            filledTemplate.getMax().getX(),
            0,
            filledTemplate.getMax().getY(),
            0,
            true
        );
        filledTemplate.setAreaXY(
            0,
            filledTemplate.getMax().getY(),
            0,
            filledTemplate.getMax().getY(),
            filledTemplate.getMax().getZ(),
            true
        );
        return true;
    }
}
