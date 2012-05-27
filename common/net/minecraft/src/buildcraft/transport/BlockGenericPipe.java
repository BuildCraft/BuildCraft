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
import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityLiving;
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
import net.minecraft.src.buildcraft.api.IPipe;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.tools.IToolWrench;
import net.minecraft.src.buildcraft.core.BlockIndex;
import net.minecraft.src.buildcraft.core.CoreProxy;
import net.minecraft.src.buildcraft.core.PersistentTile;
import net.minecraft.src.buildcraft.core.PersistentWorld;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.forge.ITextureProvider;

public class BlockGenericPipe extends BlockContainer implements
		IBlockPipe, ITextureProvider {

	/** Defined subprograms **************************************************/

	public BlockGenericPipe(int i) {
		super(i, Material.glass);

	}

	@Override
	public int getRenderType() {
		return BuildCraftCore.pipeModel;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
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

		TileEntity tile1 = world.getBlockTileEntity(i, j, k);

		if (Utils.checkPipesConnections(world, tile1, i - 1, j, k)) {
			setBlockBounds(0.0F, Utils.pipeMinPos, Utils.pipeMinPos,
					Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb,
					arraylist);
		}

		if (Utils.checkPipesConnections(world, tile1, i + 1, j, k)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos,
					Utils.pipeMinPos, 1.0F, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb,
					arraylist);
		}

		if (Utils.checkPipesConnections(world, tile1, i, j - 1, k)) {
			setBlockBounds(Utils.pipeMinPos, 0.0F, Utils.pipeMinPos,
					Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb,
					arraylist);
		}

		if (Utils.checkPipesConnections(world, tile1, i, j + 1, k)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos,
					Utils.pipeMinPos, Utils.pipeMaxPos, 1.0F, Utils.pipeMaxPos);
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb,
					arraylist);
		}

		if (Utils.checkPipesConnections(world, tile1, i, j, k - 1)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, 0.0F,
					Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.getCollidingBoundingBoxes(world, i, j, k, axisalignedbb,
					arraylist);
		}

		if (Utils.checkPipesConnections(world, tile1, i, j, k + 1)) {
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

		TileEntity tile1 = world.getBlockTileEntity(i, j, k);

		if (Utils.checkPipesConnections(world, tile1, i - 1, j, k))
			xMin = 0.0F;

		if (Utils.checkPipesConnections(world, tile1, i + 1, j, k))
			xMax = 1.0F;

		if (Utils.checkPipesConnections(world, tile1, i, j - 1, k))
			yMin = 0.0F;

		if (Utils.checkPipesConnections(world, tile1, i, j + 1, k))
			yMax = 1.0F;

		if (Utils.checkPipesConnections(world, tile1, i, j, k - 1))
			zMin = 0.0F;

		if (Utils.checkPipesConnections(world, tile1, i, j, k + 1))
			zMax = 1.0F;

		return AxisAlignedBB.getBoundingBoxFromPool((double) i + xMin,
				(double) j + yMin, (double) k + zMin, (double) i + xMax,
				(double) j + yMax, (double) k + zMax);
	}

	@SuppressWarnings({ "all" })
	// @Override (client only)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int i,
			int j, int k) {
		return getCollisionBoundingBoxFromPool(world, i, j, k);
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, int i, int j,
			int k, Vec3D vec3d, Vec3D vec3d1) {
		float xMin = Utils.pipeMinPos, xMax = Utils.pipeMaxPos, yMin = Utils.pipeMinPos, yMax = Utils.pipeMaxPos, zMin = Utils.pipeMinPos, zMax = Utils.pipeMaxPos;

		TileEntity tile1 = world.getBlockTileEntity(i, j, k);

		if (Utils.checkPipesConnections(world, tile1, i - 1, j, k))
			xMin = 0.0F;

		if (Utils.checkPipesConnections(world, tile1, i + 1, j, k))
			xMax = 1.0F;

		if (Utils.checkPipesConnections(world, tile1, i, j - 1, k))
			yMin = 0.0F;

		if (Utils.checkPipesConnections(world, tile1, i, j + 1, k))
			yMax = 1.0F;

		if (Utils.checkPipesConnections(world, tile1, i, j, k - 1))
			zMin = 0.0F;

		if (Utils.checkPipesConnections(world, tile1, i, j, k + 1))
			zMax = 1.0F;

		setBlockBounds(xMin, yMin, zMin, xMax, yMax, zMax);

		MovingObjectPosition r = super.collisionRayTrace(world, i, j, k, vec3d,
				vec3d1);

		setBlockBounds(0, 0, 0, 1, 1, 1);

		return r;
	}

	public static void removePipe (Pipe pipe) {
		if (pipe == null)
			return;

		if (isValid (pipe))
			pipe.onBlockRemoval ();

		World world = pipe.worldObj;

		if (world == null)
			return;

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

	@Override
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
			return;

        int i1 = quantityDropped(world.rand);
        for(int j1 = 0; j1 < i1; j1++)
        {
            if(world.rand.nextFloat() > f)
				continue;

            Pipe pipe = getPipe(world, i, j, k);

            if (pipe == null)
				pipe = pipeRemoved.get(new BlockIndex (i, j, k));

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
	public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
		super.onNeighborBlockChange(world, i, j, k, l);

		Pipe pipe = getPipe(world, i, j, k);

		if (isValid (pipe))
			pipe.container.scheduleNeighborChange();
	}

	@Override
	public void onBlockPlaced(World world, int i, int j, int k, int l) {
		super.onBlockPlaced(world, i, j, k, l);

		Pipe pipe = getPipe(world, i, j, k);

		if (isValid (pipe))
			pipe.onBlockPlaced();
	}

    @Override
	public void onBlockPlacedBy(World world, int i, int j, int k, EntityLiving entityliving) {
    
        if(APIProxy.isClient(world))
			return;

        TileGenericPipe tile = (TileGenericPipe)world.getBlockTileEntity(i, j, k);
        if(entityliving instanceof EntityPlayer)
        	tile.setOwner((EntityPlayer)entityliving);
    	
    }
	
	@Override
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		super.blockActivated(world, i, j, k, entityplayer);

		world.notifyBlocksOfNeighborChange(i, j, k,
				BuildCraftTransport.genericPipeBlock.blockID);

		Pipe pipe = getPipe(world, i, j, k);

		if (isValid(pipe)) {

			/// Right click while sneaking without wrench to strip equipment from the pipe.
			if(entityplayer.isSneaking() &&
					(entityplayer.getCurrentEquippedItem() == null
					|| !(entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench))) {

				if(pipe.hasGate() || pipe.isWired())
					return stripEquipment(pipe);

			} else if (entityplayer.getCurrentEquippedItem() == null) {

				// Fall through the end of the test

			} else if (entityplayer.getCurrentEquippedItem().getItem() instanceof ItemPipe)
				return false;
			else if (entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench ){
				//Only check the instance at this point. Call the IToolWrench interface callbacks for the individual pipe/logic calls
				return pipe.blockActivated (world, i, j, k, entityplayer);
			} else if (entityplayer.getCurrentEquippedItem().getItem() == BuildCraftTransport.redPipeWire) {
				if (!pipe.wireSet [IPipe.WireColor.Red.ordinal()]) {
					pipe.wireSet [IPipe.WireColor.Red.ordinal()] = true;
					entityplayer.getCurrentEquippedItem().splitStack(1);
					world.markBlockNeedsUpdate(i, j, k);

					return true;
				}
			} else if (entityplayer.getCurrentEquippedItem().getItem() == BuildCraftTransport.bluePipeWire) {
				if (!pipe.wireSet [IPipe.WireColor.Blue.ordinal()]) {
					pipe.wireSet [IPipe.WireColor.Blue.ordinal()] = true;
					entityplayer.getCurrentEquippedItem().splitStack(1);
					world.markBlockNeedsUpdate(i, j, k);

					return true;
				}
			} else if (entityplayer.getCurrentEquippedItem().getItem() == BuildCraftTransport.greenPipeWire) {
				if (!pipe.wireSet [IPipe.WireColor.Green.ordinal()]) {
					pipe.wireSet [IPipe.WireColor.Green.ordinal()] = true;
					entityplayer.getCurrentEquippedItem().splitStack(1);
					world.markBlockNeedsUpdate(i, j, k);

					return true;
				}
			} else if (entityplayer.getCurrentEquippedItem().getItem() == BuildCraftTransport.yellowPipeWire) {
				if (!pipe.wireSet [IPipe.WireColor.Yellow.ordinal()]) {
					pipe.wireSet [IPipe.WireColor.Yellow.ordinal()] = true;
					entityplayer.getCurrentEquippedItem().splitStack(1);
					world.markBlockNeedsUpdate(i, j, k);

					return true;
				}
			}
			else if (entityplayer.getCurrentEquippedItem().itemID == BuildCraftTransport.pipeGate.shiftedIndex
					|| entityplayer.getCurrentEquippedItem().itemID == BuildCraftTransport.pipeGateAutarchic.shiftedIndex)
				if (!pipe.hasInterface()) {

					pipe.gate = new GateVanilla(pipe, entityplayer.getCurrentEquippedItem());
					entityplayer.getCurrentEquippedItem().splitStack(1);
					world.markBlockNeedsUpdate(i, j, k);

					return true;
				}

			if (pipe.hasGate()) {
				pipe.gate.openGui(entityplayer);

				return true;
			} else
				return pipe.blockActivated (world, i, j, k, entityplayer);
		}

		return false;
	}

	private boolean stripEquipment(Pipe pipe) {
		
		// Try to strip wires first, starting with yellow.
		for(IPipe.WireColor color : IPipe.WireColor.values())
			if(pipe.wireSet[color.reverse().ordinal()]) {
				if(!APIProxy.isRemote())
					dropWire(color.reverse(), pipe.worldObj, pipe.xCoord, pipe.yCoord, pipe.zCoord);
				pipe.wireSet[color.reverse().ordinal()] = false;
				pipe.worldObj.markBlockNeedsUpdate(pipe.xCoord, pipe.yCoord, pipe.zCoord);
				return true;
			}

		// Try to strip gate next
		if(pipe.hasGate()) {
			if(!APIProxy.isRemote())
				pipe.gate.dropGate(pipe.worldObj, pipe.xCoord, pipe.yCoord, pipe.zCoord);
			pipe.resetGate();
			return true;
		}

		return false;
	}

	/**
	 * Drops a pipe wire item of the passed color.
	 * @param color
	 */
	private void dropWire(IPipe.WireColor color, World world, int i, int j, int k) {

		Item wireItem;
		switch(color) {
		case Red:
			wireItem = BuildCraftTransport.redPipeWire;
			break;
		case Blue:
			wireItem = BuildCraftTransport.bluePipeWire;
			break;
		case Green:
			wireItem = BuildCraftTransport.greenPipeWire;
			break;
		default:
			wireItem = BuildCraftTransport.yellowPipeWire;
		}
		Utils.dropItems(world, new ItemStack(wireItem), i, j, k);

	}

	@Override
	public void prepareTextureFor (IBlockAccess blockAccess, int i, int j, int k, Orientations connection) {
		Pipe pipe = getPipe(blockAccess, i, j, k);

		if (isValid (pipe))
			pipe.prepareTextureFor(connection);
	}

	@SuppressWarnings({ "all" })
	public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		Pipe pipe = getPipe(iblockaccess, i, j, k);
		if (!isValid(pipe)) {
			CoreProxy.BindTexture(BuildCraftCore.customBuildCraftTexture);
			return 0;
		}
		int pipeTexture = pipe.getPipeTexture();
		if (pipeTexture > 255){
			CoreProxy.BindTexture(BuildCraftCore.externalBuildCraftTexture);
			return pipeTexture - 256;
		}
		CoreProxy.BindTexture(BuildCraftCore.customBuildCraftTexture);
		return pipeTexture;
	}
	
	@Override
	public void onEntityCollidedWithBlock(World world, int i, int j, int k, Entity entity) {
		super.onEntityCollidedWithBlock(world, i, j, k, entity);

		Pipe pipe = getPipe(world, i, j, k);

		if (isValid (pipe))
			pipe.onEntityCollidedWithBlock(entity);
	}

	@Override
	public boolean isPoweringTo(IBlockAccess iblockaccess, int x, int y, int z, int l) {
		Pipe pipe = getPipe(iblockaccess, x, y, z);

		if (isValid (pipe))
			return pipe.isPoweringTo(l);
		else
			return false;
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public boolean isIndirectlyPoweringTo(World world, int i, int j, int k, int l) {
		Pipe pipe = getPipe(world, i, j, k);

		if (isValid (pipe))
			return pipe.isIndirectlyPoweringTo(l);
		else
			return false;
	}

	@SuppressWarnings({ "all" })
	// @Override (client only)
	public void randomDisplayTick(World world, int i, int j, int k, Random random) {
		Pipe pipe = getPipe(world, i, j, k);

		if (isValid (pipe))
			pipe.randomDisplayTick(random);
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

		if (tile == null || !tile.isValid() || !(tile instanceof Pipe))
			return null;
		else
			return (Pipe) tile;
	}

	public static boolean isFullyDefined (Pipe pipe) {
		return pipe != null && pipe.transport != null && pipe.logic != null;
	}

	public static boolean isValid (Pipe pipe) {
		return isFullyDefined(pipe)	&& pipe.isValid();
	}
}
