package buildcraft.transport.block;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.transport.TransportGuis;
import buildcraft.transport.tile.TileFilteredBuffer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockFilteredBuffer extends BlockBCTile_Neptune {
    public BlockFilteredBuffer(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileFilteredBuffer();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TransportGuis.FILTERED_BUFFER.openGUI(player, pos);
        }
        return true;
    }
}
