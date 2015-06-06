package buildcraft.silicon;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
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
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		if (super.onBlockActivated(world, i, j, k, entityplayer, par6, par7, par8, par9)) {
			return true;
		}

		if (!world.isRemote) {
			entityplayer.openGui(BuildCraftSilicon.instance, 10, world, i, j, k);
		}
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TilePackager();
	}
}
