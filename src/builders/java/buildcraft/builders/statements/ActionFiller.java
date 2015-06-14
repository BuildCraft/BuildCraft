/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.statements;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.statements.IActionExternal;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.builders.tile.TileFiller;
import buildcraft.core.builders.patterns.FillerPattern;
import buildcraft.core.statements.BCStatement;

public class ActionFiller extends BCStatement implements IActionExternal {

    public final FillerPattern pattern;

    public ActionFiller(FillerPattern pattern) {
        super("filler:" + pattern.getUniqueTag());
        this.pattern = pattern;
    }

    @Override
    public String getDescription() {
        return "Pattern: " + pattern.getDescription();
    }

    @Override
    public TextureAtlasSprite getGuiSprite() {
        return pattern.getGuiSprite();
    }

    @Override
    public void actionActivate(TileEntity target, EnumFacing side, IStatementContainer source, IStatementParameter[] parameters) {
        if (target instanceof TileFiller) {
            ((TileFiller) target).setPattern(pattern);
        }
    }
}
