/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.schematics;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;

public class SchematicSignLike extends SchematicTile {
    boolean isWall;

    public SchematicSignLike(boolean isWall) {
        this.isWall = isWall;
    }

    @Override
    public void rotateLeft(IBuilderContext context) {
        if (!isWall) {
            int meta = state.getBlock().getMetaFromState(state);
            double angle = (meta * 360.0) / 16.0;
            angle += 90.0;
            if (angle >= 360) {
                angle -= 360;
            }
            meta = (int) (angle / 360.0 * 16.0);
            state = state.getBlock().getStateFromMeta(meta);
        } else {
            super.rotateLeft(context);
        }
    }
}
