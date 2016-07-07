package buildcraft.factory.block;

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
import buildcraft.factory.FactoryGuis;
import buildcraft.factory.tile.TileChute;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;

public class BlockChute extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final BuildCraftProperty<Boolean> CONNECTED_UP = BuildCraftProperties.CONNECTED_UP;
    public static final BuildCraftProperty<Boolean> CONNECTED_DOWN = BuildCraftProperties.CONNECTED_DOWN;
    public static final BuildCraftProperty<Boolean> CONNECTED_EAST = BuildCraftProperties.CONNECTED_EAST;
    public static final BuildCraftProperty<Boolean> CONNECTED_WEST = BuildCraftProperties.CONNECTED_WEST;
    public static final BuildCraftProperty<Boolean> CONNECTED_NORTH = BuildCraftProperties.CONNECTED_NORTH;
    public static final BuildCraftProperty<Boolean> CONNECTED_SOUTH = BuildCraftProperties.CONNECTED_SOUTH;

    public static final Map<EnumFacing, BuildCraftProperty<Boolean>> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;

    public BlockChute(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileChute();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            FactoryGuis.CHUTE.openGUI(player, pos);
        }
        return true;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.addAll(CONNECTED_MAP.values());
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        for (EnumFacing side : EnumFacing.values()) {
            boolean has = TileChute.hasInventoryAtPosition(world, pos.offset(side), side);
            state = state.withProperty(CONNECTED_MAP.get(side), has && side != state.getValue(getFacingProperty()));
        }
        return state;
    }

    // IBlockWithFacing

    @Override
    public boolean canPlacedVertical() {
        return true;
    }
}
