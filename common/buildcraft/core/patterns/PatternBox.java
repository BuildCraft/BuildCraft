/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.patterns;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IBox;
import buildcraft.api.filler.FilledTemplate;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.containers.IFillerStatementContainer;

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
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        IBox box = filler.getBox();
        if (box == null) {
            return null;
        }
        FilledTemplate tpl = new FilledTemplate(box);
        int mx = tpl.size.getX() - 1;
        int my = tpl.size.getY() - 1;
        int mz = tpl.size.getZ() - 1;

        // Plane YZ
        tpl.fillPlaneYZ(0);
        tpl.fillPlaneYZ(mx);

        // Plane XZ
        tpl.fillPlaneXZ(0);
        tpl.fillPlaneXZ(my);

        tpl.fillPlaneXY(0);
        tpl.fillPlaneXY(mz);

        return tpl;
    }
}
