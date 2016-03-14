/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.builders.schematics;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

import java.util.List;

public class SchematicCustomStack extends SchematicBlock {

    final ItemStack customStack;

    public SchematicCustomStack(ItemStack customStack) {
        this.customStack = customStack;
    }

    @Override
    public void getRequirementsForPlacement(IBuilderContext context, List<ItemStack> requirements) {
        requirements.add(customStack.copy());
    }

    @Override
    public void storeRequirements(IBuilderContext context, BlockPos pos) {
        // cancel requirements reading
    }
}
