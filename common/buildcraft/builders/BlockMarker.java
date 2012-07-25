/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.builders;

import java.util.ArrayList;

import buildcraft.BuildCraftBuilders;
import buildcraft.BuildCraftCore;
import buildcraft.builders.BuildersProxy;
import buildcraft.core.DefaultProps;
import buildcraft.core.Utils;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.TileEntity;
import net.minecraft.src.Vec3D;
import net.minecraft.src.World;
import net.minecraft.src.forge.ITextureProvider;

public class BlockMarker extends BlockContainer implements ITextureProvider {

	public BlockMarker(int i) {
		super(i, Material.circuits);

		blockIndexInTexture = 3 * 16 + 9;

		setLightValue(0.5F);
	}

	@SuppressWarnings({ "all" })
	// @Override (client only)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int i, int j, int k) {
		int meta = world.getBlockMetadata(i, j, k);

		double w = 0.15;
		double h = 0.65;

		switch (meta) {
		case 0:
			return AxisAlignedBB.getBoundingBoxFromPool(i + 0.5 - w, j + 1 - h, k + 0.5 - w, i + 0.5 + w, j + 1, k + 0.5 + w);
		case 5:
			return AxisAlignedBB.getBoundingBoxFromPool(i + 0.5 - w, j, k + 0.5 - w, i + 0.5 + w, j + h, k + 0.5 + w);
		case 3:
			return AxisAlignedBB.getBoundingBoxFromPool(i + 0.5 - w, j + 0.5 - w, k, i + 0.5 + w, j + 0.5 + w, k + h);
		case 4:
			return AxisAlignedBB.getBoundingBoxFromPool(i + 0.5 - w, j + 0.5 - w, k + 1 - h, i + 0.5 + w, j + 0.5 + w, k + 1);
		case 1:
			return AxisAlignedBB.getBoundingBoxFromPool(i, j + 0.5 - w, k + 0.5 - w, i + h, j + 0.5 + w, k + 0.5 + w);
		default:
			return AxisAlignedBB.getBoundingBoxFromPool(i + 1 - h, j + 0.5 - w, k + 0.5 - w, i + 1, j + 0.5 + w, k + 0.5 + w);
		}
	}

	@Override
	public int getRenderType() {
		return BuildCraftCore.markerModel;
	}

	public boolean isACube() {
		return false;
	}

	@Override
	public TileEntity getBlockEntity() {
		return new TileMarker();
	}

	@Override
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		((TileMarker) world.getBlockTileEntity(i, j, k)).tryConnection();
		return true;
	}

	@Override
	public void onBlockRemoval(World world, int i, int j, int k) {
		Utils.preDestroyBlock(world, i, j, k);

		super.onBlockRemoval(world, i, j, k);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {
		return null;
	}

	@Override
	public boolean isOpaqueCube() {
		return Block.torchWood.isOpaqueCube();
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
		((TileMarker) world.getBlockTileEntity(i, j, k)).switchSignals();

		if (dropTorchIfCantStay(world, i, j, k)) {
			int i1 = world.getBlockMetadata(i, j, k);
			boolean flag = false;
			if (!BuildersProxy.canPlaceTorch(world, i - 1, j, k) && i1 == 1) {
				flag = true;
			}
			if (!BuildersProxy.canPlaceTorch(world, i + 1, j, k) && i1 == 2) {
				flag = true;
			}
			if (!BuildersProxy.canPlaceTorch(world, i, j, k - 1) && i1 == 3) {
				flag = true;
			}
			if (!BuildersProxy.canPlaceTorch(world, i, j, k + 1) && i1 == 4) {
				flag = true;
			}
			if (!BuildersProxy.canPlaceTorch(world, i, j - 1, k) && i1 == 5) {
				flag = true;
			}
			if (!BuildersProxy.canPlaceTorch(world, i, j + 1, k) && i1 == 0) {
				flag = true;
			}
			if (flag) {
				dropBlockAsItem(world, i, j, k, BuildCraftBuilders.markerBlock.blockID, 0);
				world.setBlockWithNotify(i, j, k, 0);
			}
		}
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, int i, int j, int k, Vec3D vec3d, Vec3D vec3d1) {
		return Block.torchWood.collisionRayTrace(world, i, j, k, vec3d, vec3d1);
	}

	@Override
	public boolean canPlaceBlockAt(World world, int i, int j, int k) {
		if (BuildersProxy.canPlaceTorch(world, i - 1, j, k)) {
			return true;
		}
		if (BuildersProxy.canPlaceTorch(world, i + 1, j, k)) {
			return true;
		}
		if (BuildersProxy.canPlaceTorch(world, i, j, k - 1)) {
			return true;
		}
		if (BuildersProxy.canPlaceTorch(world, i, j, k + 1)) {
			return true;
		}
		if (BuildersProxy.canPlaceTorch(world, i, j - 1, k)) {
			return true;
		}

		return BuildersProxy.canPlaceTorch(world, i, j + 1, k);
	}

	@Override
	public void onBlockPlaced(World world, int i, int j, int k, int l) {
		super.onBlockPlaced(world, i, j, k, l);
		int i1 = world.getBlockMetadata(i, j, k);
		if (l == 1 && BuildersProxy.canPlaceTorch(world, i, j - 1, k)) {
			i1 = 5;
		}
		if (l == 2 && BuildersProxy.canPlaceTorch(world, i, j, k + 1)) {
			i1 = 4;
		}
		if (l == 3 && BuildersProxy.canPlaceTorch(world, i, j, k - 1)) {
			i1 = 3;
		}
		if (l == 4 && BuildersProxy.canPlaceTorch(world, i + 1, j, k)) {
			i1 = 2;
		}
		if (l == 5 && BuildersProxy.canPlaceTorch(world, i - 1, j, k)) {
			i1 = 1;
		}
		if (l == 0 && BuildersProxy.canPlaceTorch(world, i, j + 1, k)) {
			i1 = 0;
		}
		world.setBlockMetadataWithNotify(i, j, k, i1);
	}

	@Override
	public void onBlockAdded(World world, int i, int j, int k) {
		super.onBlockAdded(world, i, j, k);

		if (BuildersProxy.canPlaceTorch(world, i - 1, j, k)) {
			world.setBlockMetadataWithNotify(i, j, k, 1);
		} else if (BuildersProxy.canPlaceTorch(world, i + 1, j, k)) {
			world.setBlockMetadataWithNotify(i, j, k, 2);
		} else if (BuildersProxy.canPlaceTorch(world, i, j, k - 1)) {
			world.setBlockMetadataWithNotify(i, j, k, 3);
		} else if (BuildersProxy.canPlaceTorch(world, i, j, k + 1)) {
			world.setBlockMetadataWithNotify(i, j, k, 4);
		} else if (BuildersProxy.canPlaceTorch(world, i, j - 1, k)) {
			world.setBlockMetadataWithNotify(i, j, k, 5);
		} else if (BuildersProxy.canPlaceTorch(world, i, j + 1, k)) {
			world.setBlockMetadataWithNotify(i, j, k, 0);
		}

		dropTorchIfCantStay(world, i, j, k);
	}

	private boolean dropTorchIfCantStay(World world, int i, int j, int k) {
		if (!canPlaceBlockAt(world, i, j, k)) {
			dropBlockAsItem(world, i, j, k, BuildCraftBuilders.markerBlock.blockID, 0);
			world.setBlockWithNotify(i, j, k, 0);
			return false;
		} else {
			return true;
		}
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		itemList.add(new ItemStack(this));
	}
}
