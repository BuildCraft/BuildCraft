package buildcraft.silicon;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.BuildCraftSilicon;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.block.BlockBuildCraft;

public class BlockPackager extends BlockBuildCraft {
    public BlockPackager() {
        super(Material.iron);

        setHardness(10F);
        setCreativeTab(BCCreativeTab.get("main"));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing face, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(world, pos, state, entityplayer, face, hitX, hitY, hitZ)) {
            return true;
        }

        if (!world.isRemote) {
            entityplayer.openGui(BuildCraftSilicon.instance, 10, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TilePackager();
    }
}
