/** 
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.transport;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.TileEntity;
import net.minecraft.src.Vec3D;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.api.APIProxy;
import net.minecraft.src.buildcraft.api.IBlockPipe;
import net.minecraft.src.buildcraft.api.IPipeConnection;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.PersistentTile;
import net.minecraft.src.buildcraft.core.PersistentWorld;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.ITextureProvider;

public class BlockGenericPipe extends BlockContainer implements
		IPipeConnection, IBlockPipe, ITextureProvider {

	/** Defined subprograms **************************************************/ 
	
	public BlockGenericPipe(int i) {
		super(i, Material.glass);

	}

	public int getRenderType() {
		return BuildCraftCore.pipeModel;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	public boolean renderAsNormalBlock() {
		return false;
	}

	public boolean isACube() {
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void getCollidingBoundingBoxes(World world, int i, int j, int k,
			AxisAlignedBB axisalignedbb, ArrayList arraylist) {
		setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos,
				Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
		super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb,
				arraylist);

		if (Utils.checkPipesConnections(world, i, j, k, i - 1, j, k)) {
			setBlockBounds(0.0F, Utils.pipeMinPos, Utils.pipeMinPos,
					Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb,
					arraylist);
		}

		if (Utils.checkPipesConnections(world, i, j, k, i + 1, j, k)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos,
					Utils.pipeMinPos, 1.0F, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb,
					arraylist);
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j - 1, k)) {
			setBlockBounds(Utils.pipeMinPos, 0.0F, Utils.pipeMinPos,
					Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb,
					arraylist);
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j + 1, k)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos,
					Utils.pipeMinPos, Utils.pipeMaxPos, 1.0F, Utils.pipeMaxPos);
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb,
					arraylist);
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j, k - 1)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, 0.0F,
					Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb,
					arraylist);
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j, k + 1)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos,
					Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos, 1.0F);
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb,
					arraylist);
		}

		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i,
			int j, int k) {
		float xMin = Utils.pipeMinPos, xMax = Utils.pipeMaxPos, yMin = Utils.pipeMinPos, yMax = Utils.pipeMaxPos, zMin = Utils.pipeMinPos, zMax = Utils.pipeMaxPos;

		if (Utils.checkPipesConnections(world, i, j, k, i - 1, j, k)) {
			xMin = 0.0F;
		}

		if (Utils.checkPipesConnections(world, i, j, k, i + 1, j, k)) {
			xMax = 1.0F;
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j - 1, k)) {
			yMin = 0.0F;
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j + 1, k)) {
			yMax = 1.0F;
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j, k - 1)) {
			zMin = 0.0F;
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j, k + 1)) {
			zMax = 1.0F;
		}

		return AxisAlignedBB.getBoundingBoxFromPool((double) i + xMin,
				(double) j + yMin, (double) k + zMin, (double) i + xMax,
				(double) j + yMax, (double) k + zMax);
	}

	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int i,
			int j, int k) {
		return getCollisionBoundingBoxFromPool(world, i, j, k);
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, int i, int j,
			int k, Vec3D vec3d, Vec3D vec3d1) {
		float xMin = Utils.pipeMinPos, xMax = Utils.pipeMaxPos, yMin = Utils.pipeMinPos, yMax = Utils.pipeMaxPos, zMin = Utils.pipeMinPos, zMax = Utils.pipeMaxPos;

		if (Utils.checkPipesConnections(world, i, j, k, i - 1, j, k)) {
			xMin = 0.0F;
		}

		if (Utils.checkPipesConnections(world, i, j, k, i + 1, j, k)) {
			xMax = 1.0F;
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j - 1, k)) {
			yMin = 0.0F;
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j + 1, k)) {
			yMax = 1.0F;
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j, k - 1)) {
			zMin = 0.0F;
		}

		if (Utils.checkPipesConnections(world, i, j, k, i, j, k + 1)) {
			zMax = 1.0F;
		}

		setBlockBounds(xMin, yMin, zMin, xMax, yMax, zMax);

		MovingObjectPosition r = super.collisionRayTrace(world, i, j, k, vec3d,
				vec3d1);

		setBlockBounds(0, 0, 0, 1, 1, 1);

		return r;
	}
	
	public static void removePipe (Pipe pipe) {
		if (pipe == null) {
			return;
		}
		
		World world = pipe.worldObj;
		
		if (world == null) {
			return;
		}
		
		int i = pipe.xCoord;
		int j = pipe.yCoord;
		int k = pipe.zCoord;
		
		if (lastRemovedDate != world.getWorldTime()) {
			lastRemovedDate = world.getWorldTime();
			pipeRemoved.clear();
		}
		
		pipeRemoved.put(new BlockIndex (i, j, k), pipe);		
		
		PersistentWorld.getWorld(world).removeTile (new BlockIndex (i, j, k));
	}

	public void onBlockRemoval(World world, int i, int j, int k) {
		Utils.preDestroyBlock(world, i, j, k);
		
		removePipe (getPipe (world, i, j, k));

		super.onBlockRemoval(world, i, j, k);
	}

	@Override
	public String getTextureFile() {
		return BuildCraftCore.customBuildCraftTexture;
	}

	@Override
	public TileEntity getBlockEntity() {
		return new TileGenericPipe();
	}
		
	@Override
	public void dropBlockAsItemWithChance(World world, int i, int j, int k,
			int l, float f, int dmg) {
		
        if(APIProxy.isClient(world))
        {
            return;
        }        
        
        int i1 = quantityDropped(world.rand);
        for(int j1 = 0; j1 < i1; j1++)
        {
            if(world.rand.nextFloat() > f)
            {
                continue;
            }
            
            Pipe pipe = getPipe(world, i, j, k);            
            
            if (pipe == null) {
            	pipe = pipeRemoved.get(new BlockIndex (i, j, k));
            }
            
            if (pipe != null) {
            	int k1 = pipe.itemID;            	            	
            	
            	if(k1 > 0)
            	{
            		pipe.dropContents ();
            		dropBlockAsItem_do(world, i, j, k, new ItemStack(k1, 1, damageDropped(l)));
            	}
            }
        }
	}
	
	@Override
	public int idDropped(int meta, Random rand, int dmg) {
		// Returns 0 to be safe - the id does not depend on the meta
		return 0;
	}
	
	/** Wrappers *************************************************************/

	@Override
	public boolean isPipeConnected(IBlockAccess blockAccess, int x1, int y1,
			int z1, int x2, int y2, int z2) {

		TileEntity tile = blockAccess.getBlockTileEntity(x2, y2, z2);

		Pipe pipe1 = getPipe(blockAccess, x1, y1, z1);
		Pipe pipe2 = getPipe(blockAccess, x2, y2, z2);

		if (!isValid(pipe1)) {
			return false;
		}

		if (isValid (pipe2) && !pipe1.transport.getClass().isAssignableFrom(
				pipe2.transport.getClass())
				&& !pipe2.transport.getClass().isAssignableFrom(
						pipe1.transport.getClass())) {
			return false;
		}

		return pipe1 != null ? pipe1.isPipeConnected(tile) : false;
	}
	
	@Override
	public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
		super.onNeighborBlockChange(world, i, j, k, l);
		
		Pipe pipe = getPipe(world, i, j, k);
		
		if (isValid (pipe)) {
			pipe.onNeighborBlockChange();
		}
	}
	
	@Override
	public void onBlockPlaced(World world, int i, int j, int k, int l) {
		super.onBlockPlaced(world, i, j, k, l);
		
		Pipe pipe = getPipe(world, i, j, k);
		
		if (isValid (pipe)) {
			pipe.onBlockPlaced();
		}
	}
	
	@Override
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		super.blockActivated(world, i, j, k, entityplayer);
		
		Pipe pipe = getPipe(world, i, j, k);
		return isValid (pipe) ? pipe.blockActivated (world, i, j, k, entityplayer) : false;
	}
	
	@Override
	public void prepareTextureFor (IBlockAccess blockAccess, int i, int j, int k, Orientations connection) {
		Pipe pipe = getPipe(blockAccess, i, j, k);
		
		if (isValid (pipe)) {
			pipe.prepareTextureFor(connection);
		}
	}
	
	public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		Pipe pipe = getPipe(iblockaccess, i, j, k);
		
		return isValid (pipe) ? pipe.getBlockTexture() : 0;
	}
	
	@Override
	public void onEntityCollidedWithBlock(World world, int i, int j, int k, Entity entity) {   
		super.onEntityCollidedWithBlock(world, i, j, k, entity);
		
		Pipe pipe = getPipe(world, i, j, k);
		
		if (isValid (pipe)) {
			pipe.onEntityCollidedWithBlock(entity);
		}
	}
	
	public boolean isPoweringTo(IBlockAccess iblockaccess, int x, int y, int z, int l) {
		Pipe pipe = getPipe(iblockaccess, x, y, z);
		
		if (isValid (pipe)) {
			return pipe.isPoweringTo(l);
		} else {
			return false;
		}
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public boolean isIndirectlyPoweringTo(World world, int i, int j, int k, int l) {
		Pipe pipe = getPipe(world, i, j, k);
		
		if (isValid (pipe)) {
			return pipe.isIndirectlyPoweringTo(l);
		} else {
			return false;
		}
	}

	public void randomDisplayTick(World world, int i, int j, int k, Random random) {
		Pipe pipe = getPipe(world, i, j, k);
		
		if (isValid (pipe)) {
			pipe.randomDisplayTick(random);
		}
	}

		
	/** Registration *********************************************************/
	
	public static TreeMap<Integer, Class<? extends Pipe>> pipes = new TreeMap<Integer, Class<? extends Pipe>>();
	
	static long lastRemovedDate = -1;
	public static TreeMap<BlockIndex, Pipe> pipeRemoved = new TreeMap<BlockIndex, Pipe>();
	
	public static Item registerPipe (int key, Class <? extends Pipe> clas) {
		Item item = new ItemPipe (key);
		
		pipes.put(item.shiftedIndex, clas);
		
		return item;
	}
	
	public static Pipe createPipe (int key) {
		try {
			return pipes.get(key).getConstructor(int.class).newInstance(key);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
		return null;
	}
	
	public static Pipe createPipe(IBlockAccess blockAccess, int i, int j, int k, int key) {		
		Pipe pipe = createPipe(key);
		pipe.setPosition(i, j, k);		
		
		return (Pipe) PersistentWorld.getWorld(blockAccess).createTile(pipe,
				new BlockIndex(i, j, k));
	}
	
	public static Pipe getPipe (IBlockAccess blockAccess, int i, int j, int k) {
		PersistentTile tile = PersistentWorld.getWorld(blockAccess).getTile(
				new BlockIndex(i, j, k));
		
		if (tile == null || !tile.isValid() || !(tile instanceof Pipe)) {
			return null;
		} else {		
			return (Pipe) tile;
		}
	}	
	
	public static boolean isFullyDefined (Pipe pipe) {
		return pipe != null && pipe.transport != null && pipe.logic != null;
	}	
	
	public static boolean isValid (Pipe pipe) {
		return isFullyDefined(pipe)	&& pipe.isValid();
	}	
}
