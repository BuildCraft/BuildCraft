package buildcraft.core.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.core.tile.TileMarkerPath;
import buildcraft.lib.block.BlockMarkerBase;
import buildcraft.lib.misc.PermissionUtil;

public class BlockMarkerPath extends BlockMarkerBase {
    public BlockMarkerPath(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileMarkerPath();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileMarkerPath) {
                TileMarkerPath marker = (TileMarkerPath) tile;
                if (PermissionUtil.hasPermission(PermissionUtil.PERM_EDIT, player, marker.getPermBlock())) {
                    marker.reverseDirection();
                }
            }
        }
        return true;
    }
}
