/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.engines;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftCore;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.api.transport.IItemPipe;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.render.ICustomHighlight;

public abstract class BlockEngineBase extends BlockBuildCraft implements ICustomHighlight {
	private static final AxisAlignedBB[][] boxes = {
			{AxisAlignedBB.getBoundingBox(0.0, 0.5, 0.0, 1.0, 1.0, 1.0), AxisAlignedBB.getBoundingBox(0.25, 0.0, 0.25, 0.75, 0.5, 0.75)}, // -Y
			{AxisAlignedBB.getBoundingBox(0.0, 0.0, 0.0, 1.0, 0.5, 1.0), AxisAlignedBB.getBoundingBox(0.25, 0.5, 0.25, 0.75, 1.0, 0.75)}, // +Y
			{AxisAlignedBB.getBoundingBox(0.0, 0.0, 0.5, 1.0, 1.0, 1.0), AxisAlignedBB.getBoundingBox(0.25, 0.25, 0.0, 0.75, 0.75, 0.5)}, // -Z
			{AxisAlignedBB.getBoundingBox(0.0, 0.0, 0.0, 1.0, 1.0, 0.5), AxisAlignedBB.getBoundingBox(0.25, 0.25, 0.5, 0.75, 0.75, 1.0)}, // +Z
			{AxisAlignedBB.getBoundingBox(0.5, 0.0, 0.0, 1.0, 1.0, 1.0), AxisAlignedBB.getBoundingBox(0.0, 0.25, 0.25, 0.5, 0.75, 0.75)}, // -X
			{AxisAlignedBB.getBoundingBox(0.0, 0.0, 0.0, 0.5, 1.0, 1.0), AxisAlignedBB.getBoundingBox(0.5, 0.25, 0.25, 1.0, 0.75, 0.75)} // +X
	};

	public BlockEngineBase() {
		super(Material.iron);
	}

	public abstract String getTexturePrefix(int meta, boolean addPrefix);

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconAbsolute(int side, int metadata) {
		return icons[metadata] == null ? icons[0][0] : icons[metadata][0];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		icons = new IIcon[16][];
		for (int meta = 0; meta < 16; meta++) {
			String prefix = getTexturePrefix(meta, false);
			if (prefix != null) {
				icons[meta] = new IIcon[1];
				icons[meta][0] = register.registerIcon(prefix + "/icon");
			}
		}
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
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof TileEngineBase) {
			return ((TileEngineBase) tile).orientation.getOpposite() == side;
		} else {
			return false;
		}
	}

	@Override
	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof TileEngineBase) {
			return ((TileEngineBase) tile).switchOrientation(false);
		} else {
			return false;
		}
	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer player, int side, float par7, float par8, float par9) {
		TileEntity tile = world.getTileEntity(i, j, k);

		BlockInteractionEvent event = new BlockInteractionEvent(player, this, world.getBlockMetadata(i, j, k));
		FMLCommonHandler.instance().bus().post(event);
		if (event.isCanceled()) {
			return false;
		}

		// Do not open guis when having a pipe in hand
		if (player.getCurrentEquippedItem() != null) {
			if (player.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
				return false;
			}
		}

		if (tile instanceof TileEngineBase) {
			return ((TileEngineBase) tile).onBlockActivated(player, ForgeDirection.getOrientation(side));
		}

		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void addCollisionBoxesToList(World wrd, int x, int y, int z, AxisAlignedBB mask, List list, Entity ent) {
		TileEntity tile = wrd.getTileEntity(x, y, z);
		if (tile instanceof TileEngineBase) {
			AxisAlignedBB[] aabbs = boxes[((TileEngineBase) tile).orientation.ordinal()];
			for (AxisAlignedBB aabb : aabbs) {
				AxisAlignedBB aabbTmp = aabb.getOffsetBoundingBox(x, y, z);
				if (mask.intersectsWith(aabbTmp)) {
					list.add(aabbTmp);
				}
			}
		} else {
			super.addCollisionBoxesToList(wrd, x, y, z, mask, list, ent);
		}
	}

	@Override
	public AxisAlignedBB[] getBoxes(World wrd, int x, int y, int z, EntityPlayer player) {
		TileEntity tile = wrd.getTileEntity(x, y, z);
		if (tile instanceof TileEngineBase) {
			return boxes[((TileEngineBase) tile).orientation.ordinal()];
		} else {
			return new AxisAlignedBB[]{AxisAlignedBB.getBoundingBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)};
		}
	}

	@Override
	public double getExpansion() {
		return 0.0075;
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World wrd, int x, int y, int z, Vec3 origin, Vec3 direction) {
		TileEntity tile = wrd.getTileEntity(x, y, z);
		if (tile instanceof TileEngineBase) {
			AxisAlignedBB[] aabbs = boxes[((TileEngineBase) tile).orientation.ordinal()];
			MovingObjectPosition closest = null;
			for (AxisAlignedBB aabb : aabbs) {
				MovingObjectPosition mop = aabb.getOffsetBoundingBox(x, y, z).calculateIntercept(origin, direction);
				if (mop != null) {
					if (closest != null && mop.hitVec.distanceTo(origin) < closest.hitVec.distanceTo(origin)) {
						closest = mop;
					} else {
						closest = mop;
					}
				}
			}
			if (closest != null) {
				closest.blockX = x;
				closest.blockY = y;
				closest.blockZ = z;
			}
			return closest;
		} else {
			return super.collisionRayTrace(wrd, x, y, z, origin, direction);
		}
	}

	@Override
	public void onPostBlockPlaced(World world, int x, int y, int z, int par5) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileEngineBase) {
			TileEngineBase engine = (TileEngineBase) tile;
			engine.orientation = ForgeDirection.UP;
			if (!engine.isOrientationValid()) {
				engine.switchOrientation(true);
			}
		}
	}

	@Override
	public int damageDropped(int i) {
		return i;
	}

	@SuppressWarnings({"all"})
	@Override
	public void randomDisplayTick(World world, int i, int j, int k, Random random) {
		TileEntity tile = world.getTileEntity(i, j, k);

		if (!(tile instanceof TileEngineBase)) {
			return;
		}

		if (((TileEngineBase) tile).getEnergyStage() == TileEngineBase.EnergyStage.OVERHEAT) {
			for (int f = 0; f < 16; f++) {
				world.spawnParticle("smoke", i + 0.4F + (random.nextFloat() * 0.2F),
						j + (random.nextFloat() * 0.5F),
						k + 0.4F + (random.nextFloat() * 0.2F),
						random.nextFloat() * 0.04F - 0.02F,
						random.nextFloat() * 0.05F + 0.02F,
						random.nextFloat() * 0.04F - 0.02F);
			}
		} else if (((TileEngineBase) tile).isBurning()) {
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
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof TileEngineBase) {
			((TileEngineBase) tile).onNeighborUpdate();
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return null;
	}

	public abstract String getUnlocalizedName(int metadata);

	public abstract TileEntity createTileEntity(World world, int metadata);

	/**
	 * Checks to see if this block has an engine tile for the given metadata.
	 */
	public abstract boolean hasEngine(int meta);
}
