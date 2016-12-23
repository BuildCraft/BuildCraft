package buildcraft.builders.block;

import java.util.Arrays;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import buildcraft.lib.misc.CapUtil;

public class BlockQuarry extends BlockBCTile_Neptune implements IBlockWithFacing {
    public BlockQuarry(Material material, String id) {
        super(material, id);
    }

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.addAll(BuildCraftProperties.CONNECTED_MAP.values());
    }

    private static boolean isConnected(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        EnumFacing facing = side;
        if (Arrays.asList(EnumFacing.HORIZONTALS).contains(facing)) {
            facing = EnumFacing.getHorizontal(side.getHorizontalIndex() + 2 + state.getValue(PROP_FACING).getHorizontalIndex());
        }
        TileEntity tile = world.getTileEntity(pos.offset(facing));
        return tile != null && tile.hasCapability(CapUtil.CAP_ITEMS, facing.getOpposite());
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        for (EnumFacing face : EnumFacing.VALUES) {
            boolean isConnected = isConnected(world, pos, state, face);
            state = state.withProperty(BuildCraftProperties.CONNECTED_MAP.get(face), isConnected);
        }
        return state;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileQuarry();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            BCBuildersGuis.QUARRY.openGUI(player, pos);
        }
        return true;
    }
}
