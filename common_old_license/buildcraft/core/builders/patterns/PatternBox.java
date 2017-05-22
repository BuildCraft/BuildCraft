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

public class PatternBox extends Pattern {

    public PatternBox() {
        super("box");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getSpriteHolder() {
        return BCCoreSprites.FILLER_BOX;
    }

    @Override
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        FilledTemplate template = new FilledTemplate(filler.getBox());
        int mx = template.size.getX() - 1;
        int my = template.size.getY() - 1;
        int mz = template.size.getZ() - 1;

        // Plane YZ
        template.fillPlaneYZ(0);
        template.fillPlaneYZ(mx);

        // Plane XZ
        for (int x = 1; x < mx; x++) {
            template.fillAxisZ(x, 0);
            template.fillAxisZ(x, my);
        }

        // Plane XY
        for (int x = 1; x < mx; x++) {
            for (int y = 1; y < my; y++) {
                template.fill(x, y, 0);
                template.fill(x, y, mz);
            }
        }

        return template;
    }
}
