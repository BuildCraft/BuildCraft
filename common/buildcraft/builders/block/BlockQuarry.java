package buildcraft.builders.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.properties.BuildCraftProperty;
import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.Arrays;
import java.util.List;

public class BlockQuarry extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final BuildCraftProperty<Boolean> CONNECTED_UP = BuildCraftProperties.CONNECTED_UP;
    public static final BuildCraftProperty<Boolean> CONNECTED_DOWN = BuildCraftProperties.CONNECTED_DOWN;
    public static final BuildCraftProperty<Boolean> CONNECTED_EAST = BuildCraftProperties.CONNECTED_EAST;
    public static final BuildCraftProperty<Boolean> CONNECTED_WEST = BuildCraftProperties.CONNECTED_WEST;
    public static final BuildCraftProperty<Boolean> CONNECTED_NORTH = BuildCraftProperties.CONNECTED_NORTH;
    public static final BuildCraftProperty<Boolean> CONNECTED_SOUTH = BuildCraftProperties.CONNECTED_SOUTH;

    public BlockQuarry(Material material, String id) {
        super(material, id);
    }

    @SuppressWarnings("Duplicates")
    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.add(CONNECTED_UP);
        properties.add(CONNECTED_DOWN);
        properties.add(CONNECTED_EAST);
        properties.add(CONNECTED_WEST);
        properties.add(CONNECTED_NORTH);
        properties.add(CONNECTED_SOUTH);
    }

    private boolean isConnected(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        EnumFacing facing = side;
        if(Arrays.asList(EnumFacing.HORIZONTALS).contains(facing)) {
            facing = EnumFacing.getHorizontal(side.getHorizontalIndex() + 2 + state.getValue(PROP_FACING).getHorizontalIndex());
        }
        TileEntity tile = world.getTileEntity(pos.offset(facing));
        return tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state
                .withProperty(CONNECTED_UP, isConnected(world, pos, state, EnumFacing.UP))
                .withProperty(CONNECTED_DOWN, isConnected(world, pos, state, EnumFacing.DOWN))
                .withProperty(CONNECTED_EAST, isConnected(world, pos, state, EnumFacing.EAST))
                .withProperty(CONNECTED_WEST, isConnected(world, pos, state, EnumFacing.WEST))
                .withProperty(CONNECTED_NORTH, isConnected(world, pos, state, EnumFacing.NORTH))
                .withProperty(CONNECTED_SOUTH, isConnected(world, pos, state, EnumFacing.SOUTH))
                ;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileQuarry();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        BCBuildersGuis.QUARRY.openGUI(player, pos);
        return true;
    }
}
