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

public class PatternFrame extends Pattern {

    public PatternFrame() {
        super("frame");
    }

    @Override
    public FilledTemplate createTemplate(IFillerStatementContainer filler, IStatementParameter[] params) {
        FilledTemplate template = new FilledTemplate(filler.getBox());

        int maxX = template.max.getX();
        int maxY = template.max.getY();
        int maxZ = template.max.getZ();

        // X axis
        template.fillLineX(1, maxX - 1, 0, 0);
        template.fillLineX(1, maxX - 1, 0, maxZ - 1);
        template.fillLineX(1, maxX - 1, maxY - 1, 0);
        template.fillLineX(1, maxX - 1, maxY - 1, maxZ - 1);

        // Y axis
        template.fillLineY(0, 1, maxY - 1, 0);
        template.fillLineY(0, 1, maxY - 1, maxZ - 1);
        template.fillLineY(maxX - 1, 1, maxY - 1, 0);
        template.fillLineY(maxX - 1, 1, maxY - 1, maxZ - 1);

        // Z axis
        template.fillAxisZ(0, 0);
        template.fillAxisZ(0, maxY);
        template.fillAxisZ(maxX, 0);
        template.fillAxisZ(maxX, maxY);

        return template;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public SpriteHolder getSpriteHolder() {
        return BCCoreSprites.FILLER_NONE;
    }
}
