/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory.schematics;

import java.util.ArrayList;
import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.api.core.IInvSlot;
import buildcraft.api.core.JavaTools;
import buildcraft.core.lib.inventory.InventoryIterator;
import buildcraft.factory.BuildCraftFactory;
import buildcraft.factory.tile.TileAutoWorkbench;

public class SchematicAutoWorkbench extends SchematicTile {

    @Override
    public void storeRequirements(IBuilderContext context, BlockPos pos) {
        TileAutoWorkbench autoWb = getTile(context, pos);
        if (autoWb != null) {
            ArrayList<ItemStack> rqs = new ArrayList<ItemStack>();
            rqs.add(new ItemStack(BuildCraftFactory.autoWorkbenchBlock));

            for (IInvSlot slot : InventoryIterator.getIterable(autoWb.craftMatrix, EnumFacing.UP)) {
                ItemStack stack = slot.getStackInSlot();
                if (stack != null) {
                    stack = stack.copy();
                    stack.stackSize = 1;
                    rqs.add(stack);
                }
            }

            storedRequirements = JavaTools.concat(storedRequirements, rqs.toArray(new ItemStack[rqs.size()]));
        }
    }

    @Override
    public void initializeFromObjectAt(IBuilderContext context, BlockPos pos) {
        super.initializeFromObjectAt(context, pos);

        tileNBT.removeTag("Items");
    }

    @Override
    public void placeInWorld(IBuilderContext context, BlockPos pos, LinkedList<ItemStack> stacks) {
        super.placeInWorld(context, pos, stacks);

        TileAutoWorkbench autoWb = getTile(context, pos);
        if (autoWb != null) {
            for (IInvSlot slot : InventoryIterator.getIterable(autoWb.craftMatrix, EnumFacing.UP)) {
                ItemStack stack = slot.getStackInSlot();
                if (stack != null) {
                    stack.stackSize = 1;
                }
            }
        }
    }

    @Override
    public BuildingStage getBuildStage() {
        return BuildingStage.STANDALONE;
    }

    private TileAutoWorkbench getTile(IBuilderContext context, BlockPos pos) {
        TileEntity tile = context.world().getTileEntity(pos);
        if (tile != null && tile instanceof TileAutoWorkbench) {
            return (TileAutoWorkbench) tile;
        }
        return null;
    }
}
