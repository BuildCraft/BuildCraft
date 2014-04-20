package buildcraft.factory;

import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.factory.render.RenderMultiblockSlave;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.List;

public class BlockRefineryComponent extends BlockBuildCraft {

	public static final String[] NAMES = new String[]{"valve", "machine_frame", "heater", "tank"};

	public static final int VALVE = 0;
	public static final int FRAME = 1;
	public static final int HEATER = 2;
	public static final int TANK = 3;

	public BlockRefineryComponent() {
		super(Material.iron, CreativeTabBuildCraft.TIER_3);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float fx, float fy, float fz) {
		if (!world.isRemote) {
			TileMultiblockSlave tile = (TileMultiblockSlave) world.getTileEntity(x, y, z);

			if (tile != null) {
				tile.onBlockActivated(player);
				return true;
			}
		}

		return !player.isSneaking();
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
		if (!world.isRemote) {
			TileMultiblockSlave tile = (TileMultiblockSlave) world.getTileEntity(x, y, z);

			if (tile != null) {
				tile.deformMultiblock();
			}
		}

		super.breakBlock(world, x, y, z, block, par6);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		for (int i = 0; i < NAMES.length; i++) {
			list.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public int getRenderType() {
		return RenderMultiblockSlave.renderID;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		switch (meta) {
			case 0:
				return new TileMultiblockValve(); // VALVE
			default:
				return new TileMultiblockSlave();
		}
	}

}
