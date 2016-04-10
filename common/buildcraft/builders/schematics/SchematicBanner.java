/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.schematics;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.blueprints.IBuilderContext;

public class SchematicBanner extends SchematicSignLike {
    public SchematicBanner(boolean isWall) {
        super(isWall);
    }

    @Override
    public void storeRequirements(IBuilderContext context, BlockPos pos) {
        super.storeRequirements(context, pos);

        // readFromNBT() always creates an empty pattern list, while
        // TileBanner.setItemValues() can create a null pattern list. This
        // causes the two returned ItemStacks to be incompatible if the
        // scanned banner was not initialized via readFromNBT().
        for (ItemStack stack : storedRequirements) {
            if (stack.hasTagCompound() && stack.getTagCompound().hasKey("BlockEntityTag")) {
                NBTTagCompound blockEntTag = stack.getTagCompound().getCompoundTag("BlockEntityTag");
                if (!blockEntTag.hasKey("Patterns")) {
                    blockEntTag.setTag("Patterns", new NBTTagList());
                }
            }
        }
    }
}
