package buildcraft.transport.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.transport.pipes.bc8.TilePipe_BC8;

public class BlockPipe extends BlockBuildCraft {
    public BlockPipe() {
        super(Material.glass);
        setCreativeTab(null);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entity, stack);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        TilePipe_BC8 pipe = new TilePipe_BC8();
        pipe.setWorldObj(world);
        return pipe;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }

    @Override
    public boolean canRenderInLayer(EnumWorldBlockLayer layer) {
        return layer == getBlockLayer() || layer == EnumWorldBlockLayer.TRANSLUCENT;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int tintIndex) {
        // Just return the tinitIndex (It IS the colour needed- PipeBlockModel sorts that all out)
        return tintIndex;
    }
}
