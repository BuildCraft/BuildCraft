/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy;

import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftEnergy;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.GuiIds;
import buildcraft.core.IItemPipe;
import buildcraft.core.proxy.CoreProxy;

public class BlockEngine extends BlockContainer {

	public BlockEngine(int i) {
		super(i, Material.iron);

		setHardness(0.5F);
		setCreativeTab(CreativeTabBuildCraft.tabBuildCraft);
		setBlockName("engineBlock");
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public int getRenderType() {
		return BuildCraftCore.blockByEntityModel;
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileEngine();
	}

	@Override
	public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side) {
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (tile instanceof TileEngine) {
			return ForgeDirection.getOrientation(((TileEngine) tile).orientation).getOpposite() == side;
		}
		return false;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		TileEngine engine = ((TileEngine) world.getBlockTileEntity(x, y, z));

		if (engine != null) {
			engine.delete();
		}
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {

		TileEngine tile = (TileEngine) world.getBlockTileEntity(i, j, k);

		// Drop through if the player is sneaking
		if (entityplayer.isSneaking())
			return false;

		// Switch orientation if whacked with a wrench.
		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, i, j, k)) {

			tile.switchOrientation();
			((IToolWrench) equipped).wrenchUsed(entityplayer, i, j, k);
			return true;

		} else {

			// Do not open guis when having a pipe in hand
			if (entityplayer.getCurrentEquippedItem() != null)
				if (entityplayer.getCurrentEquippedItem().getItem() instanceof IItemPipe)
					return false;

			if (tile.engine instanceof EngineStone) {
				if (!CoreProxy.proxy.isRenderWorld(tile.worldObj)) {
					entityplayer.openGui(BuildCraftEnergy.instance, GuiIds.ENGINE_STONE, world, i, j, k);
				}
				return true;

			} else if (tile.engine instanceof EngineIron) {
				if (!CoreProxy.proxy.isRenderWorld(tile.worldObj)) {
					entityplayer.openGui(BuildCraftEnergy.instance, GuiIds.ENGINE_IRON, world, i, j, k);
				}
				return true;
			}

		}

		return false;
	}

	@Override
	public void onPostBlockPlaced(World world, int x, int y, int z, int par5) {
		TileEngine tile = (TileEngine) world.getBlockTileEntity(x, y, z);
		tile.orientation = ForgeDirection.UP.ordinal();
		tile.switchOrientation();
	}

	@Override
	public int damageDropped(int i) {
		return i;
	}

	@SuppressWarnings({ "all" })
	@Override
	public void randomDisplayTick(World world, int i, int j, int k, Random random) {
		TileEngine tile = (TileEngine) world.getBlockTileEntity(i, j, k);

		if (!tile.isBurning())
			return;

		float f = (float) i + 0.5F;
		float f1 = (float) j + 0.0F + (random.nextFloat() * 6F) / 16F;
		float f2 = (float) k + 0.5F;
		float f3 = 0.52F;
		float f4 = random.nextFloat() * 0.6F - 0.3F;

		world.spawnParticle("reddust", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
		world.spawnParticle("reddust", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
		world.spawnParticle("reddust", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
		world.spawnParticle("reddust", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubBlocks(int blockid, CreativeTabs par2CreativeTabs, List itemList) {
		itemList.add(new ItemStack(this, 1, 0));
		itemList.add(new ItemStack(this, 1, 1));
		itemList.add(new ItemStack(this, 1, 2));
	}

	@Override
	public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
		TileEngine tile = (TileEngine) world.getBlockTileEntity(i, j, k);

		if (tile != null) {
			tile.checkRedstonePower();
		}
	}
}
