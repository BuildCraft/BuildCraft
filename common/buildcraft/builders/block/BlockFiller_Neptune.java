package buildcraft.builders.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.enums.EnumFillerPattern;
import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.builders.BCBuildersGuis;
import buildcraft.builders.tile.TileFiller_Neptune;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithFacing;

public class BlockFiller_Neptune extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final IProperty<EnumFillerPattern> PATTERN_TYPE = BuildCraftProperties.FILLER_PATTERN;

    public BlockFiller_Neptune(Material material, String id) {
        super(material, id);
        setDefaultState(getDefaultState().withProperty(PATTERN_TYPE, EnumFillerPattern.NONE));
    }

    // BlockState

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        super.addProperties(properties);
        properties.add(PATTERN_TYPE);
    }

    // Others

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        EnumFacing orientation = placer.getHorizontalFacing();
        world.setBlockState(pos, state.withProperty(PROP_FACING, orientation.getOpposite()));
        super.onBlockPlacedBy(world, pos, state, placer, stack);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileFiller_Neptune();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            BCBuildersGuis.FILLER.openGUI(player, pos);
        }
        return true;
    }
}
