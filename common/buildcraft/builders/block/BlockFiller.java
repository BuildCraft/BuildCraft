package buildcraft.builders.block;

import buildcraft.api.enums.EnumFillerPattern;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.tile.TileFiller;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;
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

import java.util.List;

public class BlockFiller extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final IProperty<EnumFillerPattern> PATTERN = BuildCraftProperties.FILLER_PATTERN;

    public BlockFiller(Material material, String id) {
        super(material, id);
        setDefaultState(getDefaultState().withProperty(PATTERN, EnumFillerPattern.NONE));
    }

    // BlockState

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.add(PATTERN);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileFiller) {
            TileFiller filler = (TileFiller) tile;
            return state.withProperty(PATTERN, EnumFillerPattern.NONE); // FIXME
        }
        return state;
    }

    // Others

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileFiller();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            BCBuildersGuis.FILLER.openGUI(player, pos);
        }
        return true;
    }

    @Override
    public boolean canBeRotated(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        return false;
    }
}
