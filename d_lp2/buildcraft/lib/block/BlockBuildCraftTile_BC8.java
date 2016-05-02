package buildcraft.lib.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import buildcraft.lib.tile.TileBuildCraft_BC8;

public abstract class BlockBuildCraftTile_BC8 extends BlockBuildCraftBase_BC8 implements ITileEntityProvider {
    public BlockBuildCraftTile_BC8(Material material, String id) {
        super(material, id);
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileBuildCraft_BC8) {
            TileBuildCraft_BC8 tileBC = (TileBuildCraft_BC8) tile;
            tileBC.onExplode(explosion);
        }
        super.onBlockExploded(world, pos, explosion);
    }
    
    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileBuildCraft_BC8) {
            TileBuildCraft_BC8 tileBC = (TileBuildCraft_BC8) tile;
            tileBC.onRemove();
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockDestroyedByPlayer(worldIn, pos, state);
    }
}
