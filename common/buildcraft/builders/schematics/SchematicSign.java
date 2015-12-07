/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.schematics;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;

public class SchematicSign extends SchematicTile {

    boolean isWall;

    public SchematicSign(boolean isWall) {
        this.isWall = isWall;
    }

    @Override
    public void getRequirementsForPlacement(IBuilderContext context, List<ItemStack> requirements) {
        requirements.add(new ItemStack(Items.sign));
    }

    @Override
    public void storeRequirements(IBuilderContext context, BlockPos pos) {
        // cancel requirements reading
    }

    @Override
    public void rotateLeft(IBuilderContext context) {
        if (!isWall) {
            Block standing = Blocks.standing_sign;
            int meta = standing.getMetaFromState(state);
            double angle = (meta * 360.0) / 16.0;
            angle += 90.0;
            if (angle >= 360) {
                angle -= 360;
            }
            meta = (int) (angle / 360.0 * 16.0);
            state = standing.getStateFromMeta(meta);
        } else {
            super.rotateLeft(context);
        }
    }
}
