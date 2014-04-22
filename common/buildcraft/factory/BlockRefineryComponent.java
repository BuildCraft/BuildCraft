package buildcraft.factory;

import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.factory.render.RenderMultiblockSlave;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

public class BlockRefineryComponent extends BlockBuildCraft {

	public static final String[] NAMES = new String[]{"valve", "machine_frame", "heater", "tank"};

	public static final int VALVE = 0;
	public static final int FRAME = 1;
	public static final int HEATER = 2;
	public static final int TANK = 3;

	private static IIcon[][] icons;

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
	public IIcon getIcon(int side, int meta) {
		if (meta == HEATER) {
			if (side == 1) {
				return icons[meta][1];
			}
		}

		if (meta == VALVE) {
			if (side == 1 || side == 0) {
				return icons[HEATER][0];
			}
		}

		return icons[meta][0];
	}

	@Override
	public void registerBlockIcons(IIconRegister register) {
		icons = new IIcon[4][2];

		icons[0][0] = register.registerIcon("buildcraft:refinery_component/valve");
		icons[1][0] = register.registerIcon("buildcraft:refinery_component/frame");
		icons[2][0] = register.registerIcon("buildcraft:refinery_component/heater_side");
		icons[2][1] = register.registerIcon("buildcraft:refinery_component/heater_top");
		icons[3][0] = register.registerIcon("buildcraft:refinery_component/tank");
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
