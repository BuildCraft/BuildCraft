package buildcraft.factory.block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.properties.BuildCraftProperty;
import buildcraft.api.tools.IToolWrench;

import buildcraft.factory.tile.TileFloodGate;
import buildcraft.lib.block.BlockBCTile_Neptune;

public class BlockFloodGate extends BlockBCTile_Neptune {
    public static final Map<EnumFacing, BuildCraftProperty<Boolean>> CONNECTED_MAP;

    static {
        CONNECTED_MAP = new HashMap<>(BuildCraftProperties.CONNECTED_MAP);
        CONNECTED_MAP.remove(EnumFacing.UP);
    }

    public BlockFloodGate(Material material, String id) {
        super(material, id);
    }

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.addAll(CONNECTED_MAP.values());
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileFloodGate();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileFloodGate) {
            TileFloodGate gate = (TileFloodGate) tile;
            for (EnumFacing side : CONNECTED_MAP.keySet()) {
                state = state.withProperty(CONNECTED_MAP.get(side), !gate.isSideBlocked(side));
            }
        }
        return state;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (heldItem.getItem() instanceof IToolWrench) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileFloodGate) {
                TileFloodGate gate = (TileFloodGate) tile;
                if (CONNECTED_MAP.containsKey(side)) {
                    gate.setSideBlocked(side, !gate.isSideBlocked(side));
                    return true;
                }
            }
            return false;
        }
        return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ);
    }
}
