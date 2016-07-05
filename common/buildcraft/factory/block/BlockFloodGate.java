package buildcraft.factory.block;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.properties.BuildCraftProperty;
import buildcraft.api.tools.IToolWrench;
import buildcraft.factory.tile.TileFloodGate;
import buildcraft.lib.block.BlockBCTile_Neptune;
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
import scala.actors.threadpool.Arrays;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class BlockFloodGate extends BlockBCTile_Neptune {
    public static final BuildCraftProperty<Boolean> CONNECTED_DOWN = BuildCraftProperties.CONNECTED_DOWN;
    public static final BuildCraftProperty<Boolean> CONNECTED_EAST = BuildCraftProperties.CONNECTED_EAST;
    public static final BuildCraftProperty<Boolean> CONNECTED_WEST = BuildCraftProperties.CONNECTED_WEST;
    public static final BuildCraftProperty<Boolean> CONNECTED_NORTH = BuildCraftProperties.CONNECTED_NORTH;
    public static final BuildCraftProperty<Boolean> CONNECTED_SOUTH = BuildCraftProperties.CONNECTED_SOUTH;

    public static final Map<EnumFacing, BuildCraftProperty<Boolean>> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    public BlockFloodGate(Material material, String id) {
        super(material, id);
    }

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.addAll(Arrays.asList(new IProperty[]{CONNECTED_DOWN, CONNECTED_EAST, CONNECTED_WEST, CONNECTED_NORTH, CONNECTED_SOUTH}));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileFloodGate();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileFloodGate tile = (TileFloodGate) world.getTileEntity(pos);
        for(EnumFacing side : EnumFacing.values()) {
            if(side != EnumFacing.UP) {
                state = state.withProperty(CONNECTED_MAP.get(side), !tile.isSideBlocked(side));
            }
        }
        return state;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if(heldItem != null && heldItem.getItem() instanceof IToolWrench) {
            TileFloodGate tile = (TileFloodGate) world.getTileEntity(pos);
            if(side != EnumFacing.UP) {
                tile.setSideBlocked(side, !tile.isSideBlocked(side));
                tile.sendNetworkUpdate(TileFloodGate.NET_FLOOD_GATE);
                world.markBlockRangeForRenderUpdate(pos, pos);
                return true;
            }
            return false;
        }
        return super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
    }
}
