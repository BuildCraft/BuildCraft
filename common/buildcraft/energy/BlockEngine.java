/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.IItemPipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import static net.minecraft.util.AxisAlignedBB.getBoundingBox;

public class BlockEngine extends BlockBuildCraft {

	public static final AxisAlignedBB[][] boxes = {
		{getBoundingBox(0.0, 0.5, 0.0, 1.0, 1.0, 1.0), getBoundingBox(0.25, 0.0, 0.25, 0.75, 0.5, 0.75)},// -Y
		{getBoundingBox(0.0, 0.0, 0.0, 1.0, 0.5, 1.0), getBoundingBox(0.25, 0.5, 0.25, 0.75, 1.0, 0.75)},// +Y
		{getBoundingBox(0.0, 0.0, 0.5, 1.0, 1.0, 1.0), getBoundingBox(0.25, 0.25, 0.0, 0.75, 0.75, 0.5)},// -Z
		{getBoundingBox(0.0, 0.0, 0.0, 1.0, 1.0, 0.5), getBoundingBox(0.25, 0.25, 0.5, 0.75, 0.75, 1.0)},// +Z
		{getBoundingBox(0.5, 0.0, 0.0, 1.0, 1.0, 1.0), getBoundingBox(0.0, 0.25, 0.25, 0.5, 0.75, 0.75)},// -X
		{getBoundingBox(0.0, 0.0, 0.0, 0.5, 1.0, 1.0), getBoundingBox(0.5, 0.25, 0.25, 1.0, 0.75, 0.75)} // +X
	};

	private static IIcon woodTexture;
	private static IIcon stoneTexture;
	private static IIcon ironTexture;

	public BlockEngine(CreativeTabBuildCraft creativeTab) {
		super(Material.iron, creativeTab);
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
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		woodTexture = par1IconRegister.registerIcon("buildcraft:engineWoodBottom");
		stoneTexture = par1IconRegister.registerIcon("buildcraft:engineStoneBottom");
		ironTexture = par1IconRegister.registerIcon("buildcraft:engineIronBottom");
	}

	@Override
	public int getRenderType() {
		return BuildCraftCore.blockByEntityModel;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		if (metadata == 1) {
			return new TileEngineStone();
		} else if (metadata == 2) {
			return new TileEngineIron();
		} else {
			return new TileEngineWood();
		}
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof TileEngine) {
			return ((TileEngine) tile).orientation.getOpposite() == side;
		} else {
			return false;
		}
	}

	@Override
	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof TileEngine) {
			return ((TileEngine) tile).switchOrientation(false);
		} else {
			return false;
		}
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer player, int side, float par7, float par8, float par9) {

		TileEngine tile = (TileEngine) world.getTileEntity(i, j, k);

		// Drop through if the player is sneaking
		if (player.isSneaking()) {
			return false;
		}

		// Do not open guis when having a pipe in hand
		if (player.getCurrentEquippedItem() != null) {
			if (player.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
				return false;
			}
		}

		if (tile instanceof TileEngine) {
			return tile.onBlockActivated(player, ForgeDirection.getOrientation(side));
		}

		return false;
	}

	@Override
	public void addCollisionBoxesToList(World wrd, int x, int y, int z, AxisAlignedBB mask, List list, Entity ent) {
		TileEntity tile = wrd.getTileEntity(x, y, z);
		if (tile instanceof TileEngine){
			AxisAlignedBB[] aabbs = boxes[((TileEngine)tile).orientation.ordinal()];
			for (AxisAlignedBB aabb : aabbs) {
				aabb = aabb.getOffsetBoundingBox(x, y, z);
				if (mask.intersectsWith(aabb)){
					list.add(aabb);
				}
			}
		} else {
			super.addCollisionBoxesToList(wrd, x, y, z, full, list, ent);
		}
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World wrd, int x, int y, int z, Vec3 origin, Vec3 direction) {
		TileEntity tile = wrd.getTileEntity(x, y, z);
		if (tile instanceof TileEngine){
			AxisAlignedBB[] aabbs = boxes[((TileEngine)tile).orientation.ordinal()];
			MovingObjectPosition closest = null;
			for(AxisAlignedBB aabb : aabbs){
				MovingObjectPosition mop = aabb.getOffsetBoundingBox(x, y, z).calculateIntercept(origin, direction);
				if(mop != null){
					mop.blockX = x;
					mop.blockY = y;
					mop.blockZ = z;
					if (closest != null) {
						if (mop.hitVec.distanceTo(origin) < closest.hitVec.distanceTo(origin)) {
							closest = mop;
						}
					} else {
						closest = mop;
					}
				}
			}
			return closest;
		} else {
			return super.collisionRayTrace(wrd, x, y, z, origin, direction);
		}
	}

	@Override
	public void onPostBlockPlaced(World world, int x, int y, int z, int par5) {
		TileEngine tile = (TileEngine) world.getTileEntity(x, y, z);
		tile.orientation = ForgeDirection.UP;

		if (!tile.isOrientationValid()) {
			tile.switchOrientation(true);
		}
	}

	@Override
	public int damageDropped(int i) {
		return i;
	}

	@SuppressWarnings({"all"})
	@Override
	public void randomDisplayTick(World world, int i, int j, int k, Random random) {
		TileEngine tile = (TileEngine) world.getTileEntity(i, j, k);

		if (!tile.isBurning()) {
			return;
		}

		float f = i + 0.5F;
		float f1 = j + 0.0F + (random.nextFloat() * 6F) / 16F;
		float f2 = k + 0.5F;
		float f3 = 0.52F;
		float f4 = random.nextFloat() * 0.6F - 0.3F;

		world.spawnParticle("reddust", f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
		world.spawnParticle("reddust", f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
		world.spawnParticle("reddust", f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
		world.spawnParticle("reddust", f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public void getSubBlocks(Item item, CreativeTabs par2CreativeTabs, List itemList) {
		if (par2CreativeTabs == CreativeTabBuildCraft.TIER_1.get()) {
			itemList.add(new ItemStack(this, 1, 0));
			itemList.add(new ItemStack(this, 1, 1));
		} else {
			itemList.add(new ItemStack(this, 1, 2));
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		TileEngine tile = (TileEngine) world.getTileEntity(x, y, z);

		if (tile != null) {
			tile.checkRedstonePower();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		switch (meta) {
			case 0:
				return woodTexture;
			case 1:
				return stoneTexture;
			case 2:
				return ironTexture;
			default:
				return null;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return null;
	}
}
