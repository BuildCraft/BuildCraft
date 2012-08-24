/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport;

import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import buildcraft.BuildCraftTransport;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.IPipe;
import buildcraft.core.BlockIndex;
import buildcraft.core.DefaultProps;
import buildcraft.core.ProxyCore;
import buildcraft.core.Utils;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.BlockContainer;
import net.minecraft.src.Entity;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IBlockAccess;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.Material;
import net.minecraft.src.MovingObjectPosition;
import net.minecraft.src.TileEntity;
import net.minecraft.src.Vec3;
import net.minecraft.src.World;


public class BlockGenericPipe extends BlockContainer {

	/** Defined subprograms **************************************************/

	public BlockGenericPipe(int i) {
		super(i, Material.glass);

	}

	@Override
	public int getRenderType() {
		return TransportProxyClient.pipeModel;
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
	public void addCollidingBlockToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity) {
		setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
		super.addCollidingBlockToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);

		TileEntity tile1 = world.getBlockTileEntity(i, j, k);

		if (Utils.checkPipesConnections(world, tile1, i - 1, j, k)) {
			setBlockBounds(0.0F, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.addCollidingBlockToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkPipesConnections(world, tile1, i + 1, j, k)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos, 1.0F, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.addCollidingBlockToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkPipesConnections(world, tile1, i, j - 1, k)) {
			setBlockBounds(Utils.pipeMinPos, 0.0F, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.addCollidingBlockToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkPipesConnections(world, tile1, i, j + 1, k)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, 1.0F, Utils.pipeMaxPos);
			super.addCollidingBlockToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkPipesConnections(world, tile1, i, j, k - 1)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, 0.0F, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.addCollidingBlockToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkPipesConnections(world, tile1, i, j, k + 1)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos, 1.0F);
			super.addCollidingBlockToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i, int j, int k) {
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

		return AxisAlignedBB.getBoundingBox((double) i + xMin, (double) j + yMin, (double) k + zMin, (double) i + xMax,
				(double) j + yMax, (double) k + zMax);
	}

	@SuppressWarnings({ "all" })
	// @Override (client only)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int i, int j, int k) {
		return getCollisionBoundingBoxFromPool(world, i, j, k);
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, int i, int j, int k, Vec3 vec3d, Vec3 vec3d1) {
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

		MovingObjectPosition r = super.collisionRayTrace(world, i, j, k, vec3d, vec3d1);

		setBlockBounds(0, 0, 0, 1, 1, 1);

		return r;
	}

	public static void removePipe(Pipe pipe) {
		if (pipe == null)
			return;

		if (isValid(pipe))
			pipe.onBlockRemoval();

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

		pipeRemoved.put(new BlockIndex(i, j, k), pipe);
		world.removeBlockTileEntity(i, j, k);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, int par5, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		removePipe(getPipe(world, x, y, z));
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}

	@Override
	public TileEntity createNewTileEntity(World var1) {
		return new TileGenericPipe();
	}

	@Override
	public void dropBlockAsItemWithChance(World world, int i, int j, int k, int l, float f, int dmg) {

		if (ProxyCore.proxy.isRemote(world))
			return;

		int i1 = quantityDropped(world.rand);
		for (int j1 = 0; j1 < i1; j1++) {
			if (world.rand.nextFloat() > f)
				continue;

			Pipe pipe = getPipe(world, i, j, k);

			if (pipe == null)
				pipe = pipeRemoved.get(new BlockIndex(i, j, k));

			if (pipe != null) {
				int k1 = pipe.itemID;

				if (k1 > 0) {
					pipe.dropContents();
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
	public void onNeighborBlockChange(World world, int x, int y, int z, int l) {
		super.onNeighborBlockChange(world, x, y, z, l);

		Pipe pipe = getPipe(world, x, y, z);

		if (isValid(pipe))
			pipe.container.scheduleNeighborChange();
	}


	@Override
	public void updateBlockMetadata(World world, int x, int y, int z, int par5,	float par6, float par7, float par8) {
		super.updateBlockMetadata(world, x, y, z, par5, par6, par7, par8);
		Pipe pipe = getPipe(world, x, y, z);

		if (isValid(pipe))
			pipe.onBlockPlaced();

	}

	@Override
	public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
		super.onBlockActivated(world, i, j, k, entityplayer, par6, par7, par8, par9);

		world.notifyBlocksOfNeighborChange(i, j, k, BuildCraftTransport.genericPipeBlock.blockID);

		Pipe pipe = getPipe(world, i, j, k);

		if (isValid(pipe)) {

			// / Right click while sneaking without wrench to strip equipment
			// from the pipe.
			if (entityplayer.isSneaking()
					&& (entityplayer.getCurrentEquippedItem() == null || !(entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench))) {

				if (pipe.hasGate() || pipe.isWired())
					return stripEquipment(pipe);

			} else if (entityplayer.getCurrentEquippedItem() == null) {

				// Fall through the end of the test

			} else if (entityplayer.getCurrentEquippedItem().getItem() instanceof ItemPipe)
				return false;
			else if (entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench) {
				// Only check the instance at this point. Call the IToolWrench
				// interface callbacks for the individual pipe/logic calls
				return pipe.blockActivated(world, i, j, k, entityplayer);
			} else if (entityplayer.getCurrentEquippedItem().getItem() == BuildCraftTransport.redPipeWire) {
				if (!pipe.wireSet[IPipe.WireColor.Red.ordinal()]) {
					pipe.wireSet[IPipe.WireColor.Red.ordinal()] = true;
					entityplayer.getCurrentEquippedItem().splitStack(1);
					pipe.container.scheduleRenderUpdate();
					//world.markBlockNeedsUpdate(i, j, k);

					return true;
				}
			} else if (entityplayer.getCurrentEquippedItem().getItem() == BuildCraftTransport.bluePipeWire) {
				if (!pipe.wireSet[IPipe.WireColor.Blue.ordinal()]) {
					pipe.wireSet[IPipe.WireColor.Blue.ordinal()] = true;
					entityplayer.getCurrentEquippedItem().splitStack(1);
					pipe.container.scheduleRenderUpdate();
					//world.markBlockNeedsUpdate(i, j, k);

					return true;
				}
			} else if (entityplayer.getCurrentEquippedItem().getItem() == BuildCraftTransport.greenPipeWire) {
				if (!pipe.wireSet[IPipe.WireColor.Green.ordinal()]) {
					pipe.wireSet[IPipe.WireColor.Green.ordinal()] = true;
					entityplayer.getCurrentEquippedItem().splitStack(1);
					pipe.container.scheduleRenderUpdate();
					return true;
				}
			} else if (entityplayer.getCurrentEquippedItem().getItem() == BuildCraftTransport.yellowPipeWire) {
				if (!pipe.wireSet[IPipe.WireColor.Yellow.ordinal()]) {
					pipe.wireSet[IPipe.WireColor.Yellow.ordinal()] = true;
					entityplayer.getCurrentEquippedItem().splitStack(1);
					pipe.container.scheduleRenderUpdate();
					return true;
				}
			} else if (entityplayer.getCurrentEquippedItem().itemID == BuildCraftTransport.pipeGate.shiftedIndex
					|| entityplayer.getCurrentEquippedItem().itemID == BuildCraftTransport.pipeGateAutarchic.shiftedIndex)
				if (!pipe.hasInterface()) {

					pipe.gate = new GateVanilla(pipe, entityplayer.getCurrentEquippedItem());
					entityplayer.getCurrentEquippedItem().splitStack(1);
					pipe.container.scheduleRenderUpdate();
					return true;
				}

			if (pipe.hasGate()) {
				pipe.gate.openGui(entityplayer);

				return true;
			} else
				return pipe.blockActivated(world, i, j, k, entityplayer);
		}

		return false;
	}

	private boolean stripEquipment(Pipe pipe) {

		// Try to strip wires first, starting with yellow.
		for (IPipe.WireColor color : IPipe.WireColor.values())
			if (pipe.wireSet[color.reverse().ordinal()]) {
				if (!ProxyCore.proxy.isRemote(pipe.worldObj))
					dropWire(color.reverse(), pipe.worldObj, pipe.xCoord, pipe.yCoord, pipe.zCoord);
				pipe.wireSet[color.reverse().ordinal()] = false;
				//pipe.worldObj.markBlockNeedsUpdate(pipe.xCoord, pipe.yCoord, pipe.zCoord);
				pipe.container.scheduleRenderUpdate();
				return true;
			}

		// Try to strip gate next
		if (pipe.hasGate()) {
			if (!ProxyCore.proxy.isRemote(pipe.worldObj))
				pipe.gate.dropGate(pipe.worldObj, pipe.xCoord, pipe.yCoord, pipe.zCoord);
			pipe.resetGate();
			return true;
		}

		return false;
	}

	/**
	 * Drops a pipe wire item of the passed color.
	 *
	 * @param color
	 */
	private void dropWire(IPipe.WireColor color, World world, int i, int j, int k) {

		Item wireItem;
		switch (color) {
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

	@SuppressWarnings({ "all" })
	public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {

		TileEntity tile = iblockaccess.getBlockTileEntity(i, j, k);
		if (!(tile instanceof IPipeRenderState)) return 0;
		return ((IPipeRenderState)tile).getRenderState().currentTextureIndex;


//		Pipe pipe = getPipe(iblockaccess, i, j, k);
//		if (!isValid(pipe)) {
//			CoreProxy.BindTexture(DefaultProps.TEXTURE_BLOCKS);
//			return 0;
//		}
//		int pipeTexture = pipe.getPipeTexture();
//		if (pipeTexture > 255) {
//			CoreProxy.BindTexture(DefaultProps.TEXTURE_EXTERNAL);
//			return pipeTexture - 256;
//		}
//		CoreProxy.BindTexture(DefaultProps.TEXTURE_BLOCKS);
//		return pipeTexture;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int i, int j, int k, Entity entity) {
		super.onEntityCollidedWithBlock(world, i, j, k, entity);

		Pipe pipe = getPipe(world, i, j, k);

		if (isValid(pipe))
			pipe.onEntityCollidedWithBlock(entity);
	}

	@Override
	public boolean isPoweringTo(IBlockAccess iblockaccess, int x, int y, int z, int l) {
		Pipe pipe = getPipe(iblockaccess, x, y, z);

		if (isValid(pipe))
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

		if (isValid(pipe))
			return pipe.isIndirectlyPoweringTo(l);
		else
			return false;
	}

	@SuppressWarnings({ "all" })
	// @Override (client only)
	public void randomDisplayTick(World world, int i, int j, int k, Random random) {
		Pipe pipe = getPipe(world, i, j, k);

		if (isValid(pipe))
			pipe.randomDisplayTick(random);
	}

	/** Registration *********************************************************/

	public static TreeMap<Integer, Class<? extends Pipe>> pipes = new TreeMap<Integer, Class<? extends Pipe>>();

	static long lastRemovedDate = -1;
	public static TreeMap<BlockIndex, Pipe> pipeRemoved = new TreeMap<BlockIndex, Pipe>();

	public static ItemPipe registerPipe(int key, Class<? extends Pipe> clas) {
		ItemPipe item = new ItemPipe(key);

		pipes.put(item.shiftedIndex, clas);

		Pipe dummyPipe = createPipe(item.shiftedIndex);
		if (dummyPipe != null){
			item.setTextureFile(dummyPipe.getTextureFile());
			item.setTextureIndex(dummyPipe.getTextureIndexForItem());
		}

		return item;
	}

	public static Pipe createPipe(int key) {

		try {
			Class<? extends Pipe> pipe = pipes.get(key);
			if (pipe != null) {
				return pipe.getConstructor(int.class).newInstance(key);
			} else {
				System.err.printf("[BuildCraft] Detected pipe with unknown key (" + key + "). Did you remove a buildcraft addon?\n");
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}

		return null;
	}

	public static boolean placePipe(Pipe pipe, World world, int i, int j, int k, int blockId, int meta) {
		if (world.isRemote) return true;

		boolean placed = world.setBlockAndMetadataWithNotify(i, j, k, blockId, meta);

		if (placed) {

			TileGenericPipe tile = (TileGenericPipe) world.getBlockTileEntity(i, j, k);
			tile.initialize(pipe);
		}

		return placed;
	}

	public static Pipe getPipe(IBlockAccess blockAccess, int i, int j, int k) {

		TileEntity tile = blockAccess.getBlockTileEntity(i, j, k);

		if(!(tile instanceof TileGenericPipe) || tile.isInvalid())
			return null;

		return ((TileGenericPipe)tile).pipe;
	}

	public static boolean isFullyDefined(Pipe pipe) {
		return pipe != null && pipe.transport != null && pipe.logic != null;
	}

	public static boolean isValid(Pipe pipe) {
		return isFullyDefined(pipe);
	}
}
