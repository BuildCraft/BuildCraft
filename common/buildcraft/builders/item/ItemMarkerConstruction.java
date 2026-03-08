/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.item;

import buildcraft.builders.block.BlockMarkerConstruction;
import buildcraft.builders.tile.TileArchitectTable;
import buildcraft.builders.tile.TileBuilder;
import buildcraft.builders.tile.TileMarkerConstruction;
import buildcraft.lib.item.ItemBlockBC_Neptune;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.VecUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class ItemMarkerConstruction extends ItemBlockBC_Neptune {
    public ItemMarkerConstruction(BlockMarkerConstruction block, Item.Properties properties) {
        super(block, properties);
    }

    public static boolean linkStarted(ItemStack marker) {
        return NBTUtilBC.getItemData(marker).contains("x");
    }

    public static void link(ItemStack marker, World world, BlockPos pos) {
        CompoundNBT nbt = NBTUtilBC.getItemData(marker);

        if (nbt.contains("x")) {
            int ox = nbt.getInt("x");
            int oy = nbt.getInt("y");
            int oz = nbt.getInt("z");

            TileEntity tile1 = world.getBlockEntity(new BlockPos(ox, oy, oz));

            // TODO (CHECK) is this right?
            if (!(new Vector3d(ox, oy, oz).distanceToSqr(VecUtil.convert(pos)) > 64)) {
                return;
            }

            if (tile1 instanceof TileArchitectTable) {
                TileArchitectTable architect = (TileArchitectTable) tile1;
                TileEntity tile2 = world.getBlockEntity(pos);

                if (tile1 != tile2 && tile2 != null) {
                    if (tile2 instanceof TileArchitectTable || tile2 instanceof TileMarkerConstruction || tile2 instanceof TileBuilder) {
                        // TODO Calen addSubBlueprint!!!
                        // architect.addSubBlueprint(tile2);

                        nbt.remove("x");
                        nbt.remove("y");
                        nbt.remove("z");
                    }
                }

                return;
            }
        }

        nbt.putInt("x", pos.getX());
        nbt.putInt("y", pos.getY());
        nbt.putInt("z", pos.getZ());
    }

    @Override
    // public boolean onItemUse(ItemStack marker, EntityPlayer player, Level world, BlockPos pos, EnumFacing facing, float par8, float par9, float par10)
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack marker = context.getItemInHand();

        TileEntity tile = world.getBlockEntity(pos);
        CompoundNBT nbt = NBTUtilBC.getItemData(marker);

        if (nbt.contains("x") && !(tile instanceof TileBuilder || tile instanceof TileArchitectTable || tile instanceof TileMarkerConstruction)) {

            nbt.remove("x");
            nbt.remove("y");
            nbt.remove("z");

            return ActionResultType.SUCCESS;
        } else {
            return super.useOn(context);
        }
    }
}
