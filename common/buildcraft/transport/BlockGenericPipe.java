/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.ISolidSideTile;
import buildcraft.core.BlockIndex;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.Utils;
import buildcraft.transport.render.PipeWorldRenderer;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockGenericPipe extends BlockContainer {
	static enum Part {
		Pipe,
		Gate
	}

	static class RaytraceResult {
		RaytraceResult(Part hitPart, MovingObjectPosition movingObjectPosition) {
			this.hitPart = hitPart;
			this.movingObjectPosition = movingObjectPosition;
		}

		public Part hitPart;
		public MovingObjectPosition movingObjectPosition;
	}

	private static Random rand = new Random();

	private boolean skippedFirstIconRegister;

	/* Defined subprograms ************************************************* */

	public BlockGenericPipe(int i) {
		super(i, Material.glass);

	}

	@Override
	public float getBlockHardness(World par1World, int par2, int par3, int par4) {
		return BuildCraftTransport.pipeDurability;
	}

	@Override
	public int getRenderType() {
		return TransportProxy.pipeModel;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean canBeReplacedByLeaves(World world, int x, int y, int z) {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isBlockSolidOnSide(World world, int x, int y, int z, ForgeDirection side) {
		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if (tile instanceof ISolidSideTile) {
			return ((ISolidSideTile) tile).isSolidOnSide(side);
		}
		return false;
	}

	public boolean isACube() {
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity) {
		setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
		super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);

		TileEntity tile1 = world.getBlockTileEntity(i, j, k);
		TileGenericPipe tileG = (TileGenericPipe) tile1;

		if (Utils.checkPipesConnections(world, tile1, i - 1, j, k)) {
			setBlockBounds(0.0F, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkPipesConnections(world, tile1, i + 1, j, k)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos, 1.0F, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkPipesConnections(world, tile1, i, j - 1, k)) {
			setBlockBounds(Utils.pipeMinPos, 0.0F, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkPipesConnections(world, tile1, i, j + 1, k)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, 1.0F, Utils.pipeMaxPos);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkPipesConnections(world, tile1, i, j, k - 1)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, 0.0F, Utils.pipeMaxPos, Utils.pipeMaxPos, Utils.pipeMaxPos);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkPipesConnections(world, tile1, i, j, k + 1)) {
			setBlockBounds(Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMinPos, Utils.pipeMaxPos, Utils.pipeMaxPos, 1.0F);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		}

		if (tileG != null) {
			float facadeThickness = PipeWorldRenderer.facadeThickness;

			if (tileG.hasFacade(ForgeDirection.EAST)) {
				setBlockBounds(1 - facadeThickness, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.hasFacade(ForgeDirection.WEST)) {
				setBlockBounds(0.0F, 0.0F, 0.0F, facadeThickness, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.hasFacade(ForgeDirection.UP)) {
				setBlockBounds(0.0F, 1 - facadeThickness, 0.0F, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.hasFacade(ForgeDirection.DOWN)) {
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, facadeThickness, 1.0F);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.hasFacade(ForgeDirection.SOUTH)) {
				setBlockBounds(0.0F, 0.0F, 1 - facadeThickness, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.hasFacade(ForgeDirection.NORTH)) {
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, facadeThickness);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}
		}

		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int i, int j, int k) {
		float xMin = Utils.pipeMinPos, xMax = Utils.pipeMaxPos, yMin = Utils.pipeMinPos, yMax = Utils.pipeMaxPos, zMin = Utils.pipeMinPos, zMax = Utils.pipeMaxPos;

		TileEntity tile1 = world.getBlockTileEntity(i, j, k);
		TileGenericPipe tileG = (TileGenericPipe) tile1;

		if (Utils.checkPipesConnections(world, tile1, i - 1, j, k) || (tileG != null && tileG.hasFacade(ForgeDirection.WEST))) {
			xMin = 0.0F;
		}

		if (Utils.checkPipesConnections(world, tile1, i + 1, j, k) || (tileG != null && tileG.hasFacade(ForgeDirection.EAST))) {
			xMax = 1.0F;
		}

		if (Utils.checkPipesConnections(world, tile1, i, j - 1, k) || (tileG != null && tileG.hasFacade(ForgeDirection.DOWN))) {
			yMin = 0.0F;
		}

		if (Utils.checkPipesConnections(world, tile1, i, j + 1, k) || (tileG != null && tileG.hasFacade(ForgeDirection.UP))) {
			yMax = 1.0F;
		}

		if (Utils.checkPipesConnections(world, tile1, i, j, k - 1) || (tileG != null && tileG.hasFacade(ForgeDirection.NORTH))) {
			zMin = 0.0F;
		}

		if (Utils.checkPipesConnections(world, tile1, i, j, k + 1) || (tileG != null && tileG.hasFacade(ForgeDirection.SOUTH))) {
			zMax = 1.0F;
		}

		if (tileG != null) {
			if (tileG.hasFacade(ForgeDirection.EAST) || tileG.hasFacade(ForgeDirection.WEST)) {
				yMin = 0.0F;
				yMax = 1.0F;
				zMin = 0.0F;
				zMax = 1.0F;
			}

			if (tileG.hasFacade(ForgeDirection.UP) || tileG.hasFacade(ForgeDirection.DOWN)) {
				xMin = 0.0F;
				xMax = 1.0F;
				zMin = 0.0F;
				zMax = 1.0F;
			}

			if (tileG.hasFacade(ForgeDirection.SOUTH) || tileG.hasFacade(ForgeDirection.NORTH)) {
				xMin = 0.0F;
				xMax = 1.0F;
				yMin = 0.0F;
				yMax = 1.0F;
			}
		}

		return AxisAlignedBB.getBoundingBox((double) i + xMin, (double) j + yMin, (double) k + zMin, (double) i + xMax, (double) j + yMax, (double) k + zMax);
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 origin, Vec3 direction) {
		RaytraceResult raytraceResult = doRayTrace(world, x, y, z, origin, direction);

		if (raytraceResult == null) {
			return null;
		} else {
			return raytraceResult.movingObjectPosition;
		}
	}

	public RaytraceResult doRayTrace(World world, int x, int y, int z, EntityPlayer entityPlayer) {
		double pitch = Math.toRadians(entityPlayer.rotationPitch);
		double yaw = Math.toRadians(entityPlayer.rotationYaw);

        double dirX = -Math.sin(yaw) * Math.cos(pitch);
        double dirY = -Math.sin(pitch);
        double dirZ = Math.cos(yaw) * Math.cos(pitch);

        double reachDistance = 5;

        if (entityPlayer instanceof EntityPlayerMP) {
        	reachDistance = ((EntityPlayerMP) entityPlayer).theItemInWorldManager.getBlockReachDistance();
        }

		Vec3 origin = Vec3.fakePool.getVecFromPool(entityPlayer.posX, entityPlayer.posY + 1.62 - entityPlayer.yOffset, entityPlayer.posZ);
		Vec3 direction = origin.addVector(dirX * reachDistance, dirY * reachDistance, dirZ * reachDistance);

		return doRayTrace(world, x, y, z, origin, direction);
	}

	public RaytraceResult doRayTrace(World world, int x, int y, int z, Vec3 origin, Vec3 direction) {
		float xMin = Utils.pipeMinPos, xMax = Utils.pipeMaxPos, yMin = Utils.pipeMinPos, yMax = Utils.pipeMaxPos, zMin = Utils.pipeMinPos, zMax = Utils.pipeMaxPos;

		TileEntity pipeTileEntity = world.getBlockTileEntity(x, y, z);
		Pipe pipe = getPipe(world, x, y, z);

		if (pipeTileEntity == null || !isValid(pipe)) {
			return null;
		}

		/**
		 * pipe hits along x, y, and z axis, gate (all 6 sides) [and wires+facades]
		 */
		MovingObjectPosition[] hits = new MovingObjectPosition[] { null, null, null, null, null, null, null, null, null };

		boolean needAxisCheck = false;
		boolean needCenterCheck = true;

		// check along the x axis

		if (Utils.checkPipesConnections(world, pipeTileEntity, x - 1, y, z)) {
			xMin = 0.0F;
			needAxisCheck = true;
		}

		if (Utils.checkPipesConnections(world, pipeTileEntity, x + 1, y, z)) {
			xMax = 1.0F;
			needAxisCheck = true;
		}

		if (needAxisCheck) {
			setBlockBounds(xMin, yMin, zMin, xMax, yMax, zMax);

			hits[0] = super.collisionRayTrace(world, x, y, z, origin, direction);
			xMin = Utils.pipeMinPos;
			xMax = Utils.pipeMaxPos;
			needAxisCheck = false;
			needCenterCheck = false; // center already checked through this axis
		}

		// check along the y axis

		if (Utils.checkPipesConnections(world, pipeTileEntity, x, y - 1, z)) {
			yMin = 0.0F;
			needAxisCheck = true;
		}

		if (Utils.checkPipesConnections(world, pipeTileEntity, x, y + 1, z)) {
			yMax = 1.0F;
			needAxisCheck = true;
		}

		if (needAxisCheck) {
			setBlockBounds(xMin, yMin, zMin, xMax, yMax, zMax);

			hits[1] = super.collisionRayTrace(world, x, y, z, origin, direction);
			yMin = Utils.pipeMinPos;
			yMax = Utils.pipeMaxPos;
			needAxisCheck = false;
			needCenterCheck = false; // center already checked through this axis
		}

		// check along the z axis

		if (Utils.checkPipesConnections(world, pipeTileEntity, x, y, z - 1)) {
			zMin = 0.0F;
			needAxisCheck = true;
		}

		if (Utils.checkPipesConnections(world, pipeTileEntity, x, y, z + 1)) {
			zMax = 1.0F;
			needAxisCheck = true;
		}

		if (needAxisCheck) {
			setBlockBounds(xMin, yMin, zMin, xMax, yMax, zMax);

			hits[2] = super.collisionRayTrace(world, x, y, z, origin, direction);
			zMin = Utils.pipeMinPos;
			zMax = Utils.pipeMaxPos;
			needAxisCheck = false;
			needCenterCheck = false; // center already checked through this axis
		}

		// check center (only if no axis were checked/the pipe has no connections)

		if (needCenterCheck) {
			setBlockBounds(xMin, yMin, zMin, xMax, yMax, zMax);

			hits[0] = super.collisionRayTrace(world, x, y, z, origin, direction);
		}

		// gates

		if (pipe.hasGate()) {
			for (int side = 0; side < 6; side++) {
				setBlockBoundsToGate(ForgeDirection.VALID_DIRECTIONS[side]);

				hits[3 + side] = super.collisionRayTrace(world, x, y, z, origin, direction);
			}
		}

		// TODO: check wires, facades

		// get closest hit

		double minLengthSquared = Double.POSITIVE_INFINITY;
		int minIndex = -1;

		for (int i = 0; i < hits.length; i++) {
			MovingObjectPosition hit = hits[i];
			if (hit == null) continue;

			double lengthSquared = hit.hitVec.squareDistanceTo(origin);

			if (lengthSquared < minLengthSquared) {
				minLengthSquared = lengthSquared;
				minIndex = i;
			}
		}

		// reset bounds

		setBlockBounds(0, 0, 0, 1, 1, 1);

		if (minIndex == -1) {
			return null;
		} else {
			Part hitPart;

			if (minIndex < 3) {
				hitPart = Part.Pipe;
			} else {
				hitPart = Part.Gate;
			}

			return new RaytraceResult(hitPart, hits[minIndex]);
		}
	}

	private void setBlockBoundsToGate(ForgeDirection dir) {
		float min = Utils.pipeMinPos + 0.05F;
		float max = Utils.pipeMaxPos - 0.05F;

		switch (dir) {
		case DOWN:
			setBlockBounds(min, Utils.pipeMinPos - 0.10F, min, max, Utils.pipeMinPos, max);
			break;
		case UP:
			setBlockBounds(min, Utils.pipeMaxPos, min, max, Utils.pipeMaxPos + 0.10F, max);
			break;
		case NORTH:
			setBlockBounds(min, min, Utils.pipeMinPos - 0.10F, max, max, Utils.pipeMinPos);
			break;
		case SOUTH:
			setBlockBounds(min, min, Utils.pipeMaxPos, max, max, Utils.pipeMaxPos + 0.10F);
			break;
		case WEST:
			setBlockBounds(Utils.pipeMinPos - 0.10F, min, min, Utils.pipeMinPos, max, max);
			break;
		case EAST:
			setBlockBounds(Utils.pipeMaxPos, min, min, Utils.pipeMaxPos + 0.10F, max, max);
			break;
		}
	}

	public static void removePipe(Pipe pipe) {
		if (pipe == null)
			return;

		if (isValid(pipe)) {
			pipe.onBlockRemoval();
		}

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
	public ArrayList<ItemStack> getBlockDropped(World world, int x, int y, int z, int metadata, int fortune) {

		if (CoreProxy.proxy.isRenderWorld(world))
			return null;

		ArrayList<ItemStack> list = new ArrayList<ItemStack>();

		int count = quantityDropped(metadata, fortune, world.rand);
		for (int i = 0; i < count; i++) {
			Pipe pipe = getPipe(world, x, y, z);

			if (pipe == null) {
				pipe = pipeRemoved.get(new BlockIndex(x, y, z));
			}

			if (pipe != null) {
				if (pipe.itemID > 0) {
					pipe.dropContents();
					list.add(new ItemStack(pipe.itemID, 1, damageDropped(metadata)));
				}
			}
		}
		return list;
	}
	public TileEntity createNewTileEntity(World var1) {
		return new TileGenericPipe();
	}

	@Override
	public void dropBlockAsItemWithChance(World world, int i, int j, int k, int l, float f, int dmg) {

		if (CoreProxy.proxy.isRenderWorld(world))
			return;

		int i1 = quantityDropped(world.rand);
		for (int j1 = 0; j1 < i1; j1++) {
			if (world.rand.nextFloat() > f) {
				continue;
			}

			Pipe pipe = getPipe(world, i, j, k);

			if (pipe == null) {
				pipe = pipeRemoved.get(new BlockIndex(i, j, k));
			}

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

	@SideOnly(Side.CLIENT)
	@Override
	public int idPicked(World world, int i, int j, int k) {
		Pipe pipe = getPipe(world, i, j, k);

		if (pipe == null)
			return 0;
		else
			return pipe.itemID;
	}

	/* Wrappers ************************************************************ */

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int l) {
		super.onNeighborBlockChange(world, x, y, z, l);

		Pipe pipe = getPipe(world, x, y, z);

		if (isValid(pipe)) {
			pipe.container.scheduleNeighborChange();
		}
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float par6, float par7, float par8, int meta) {
		super.onBlockPlaced(world, x, y, z, side, par6, par7, par8, meta);
		Pipe pipe = getPipe(world, x, y, z);

		if (isValid(pipe)) {
			pipe.onBlockPlaced();
		}

		return meta;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving placer, ItemStack stack) {
		super.onBlockPlacedBy(world, x, y, z, placer, stack);
		Pipe pipe = getPipe(world, x, y, z);

		if (isValid(pipe)) {
			pipe.onBlockPlacedBy(placer);
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int par6, float xOffset, float yOffset, float zOffset) {
		super.onBlockActivated(world, x, y, z, entityplayer, par6, xOffset, yOffset, zOffset);

		world.notifyBlocksOfNeighborChange(x, y, z, BuildCraftTransport.genericPipeBlock.blockID);

		Pipe pipe = getPipe(world, x, y, z);

		if (isValid(pipe)) {

			// / Right click while sneaking without wrench to strip equipment
			// from the pipe.
			if (entityplayer.isSneaking()
					&& (entityplayer.getCurrentEquippedItem() == null || !(entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench))) {

				if (pipe.hasGate() || pipe.isWired())
					return stripEquipment(pipe);

			} else if (entityplayer.getCurrentEquippedItem() == null) {

				// Fall through the end of the test
			} else if (entityplayer.getCurrentEquippedItem().itemID == Item.sign.itemID)
				// Sign will be placed anyway, so lets show the sign gui
				return false;
			else if (entityplayer.getCurrentEquippedItem().getItem() instanceof ItemPipe)
				return false;
			else if (entityplayer.getCurrentEquippedItem().getItem() instanceof IToolWrench)
				// Only check the instance at this point. Call the IToolWrench
				// interface callbacks for the individual pipe/logic calls
				return pipe.blockActivated(world, x, y, z, entityplayer);
			else if (entityplayer.getCurrentEquippedItem().getItem() == BuildCraftTransport.redPipeWire) {
				if (!pipe.wireSet[IPipe.WireColor.Red.ordinal()]) {
					pipe.wireSet[IPipe.WireColor.Red.ordinal()] = true;
					if (!entityplayer.capabilities.isCreativeMode) {
						entityplayer.getCurrentEquippedItem().splitStack(1);
					}
					pipe.signalStrength[IPipe.WireColor.Red.ordinal()] = 0;
					pipe.container.scheduleNeighborChange();
					return true;
				}
			} else if (entityplayer.getCurrentEquippedItem().getItem() == BuildCraftTransport.bluePipeWire) {
				if (!pipe.wireSet[IPipe.WireColor.Blue.ordinal()]) {
					pipe.wireSet[IPipe.WireColor.Blue.ordinal()] = true;
					if (!entityplayer.capabilities.isCreativeMode) {
						entityplayer.getCurrentEquippedItem().splitStack(1);
					}
					pipe.signalStrength[IPipe.WireColor.Blue.ordinal()] = 0;
					pipe.container.scheduleNeighborChange();
					return true;
				}
			} else if (entityplayer.getCurrentEquippedItem().getItem() == BuildCraftTransport.greenPipeWire) {
				if (!pipe.wireSet[IPipe.WireColor.Green.ordinal()]) {
					pipe.wireSet[IPipe.WireColor.Green.ordinal()] = true;
					if (!entityplayer.capabilities.isCreativeMode) {
						entityplayer.getCurrentEquippedItem().splitStack(1);
					}
					pipe.signalStrength[IPipe.WireColor.Green.ordinal()] = 0;
					pipe.container.scheduleNeighborChange();
					return true;
				}
			} else if (entityplayer.getCurrentEquippedItem().getItem() == BuildCraftTransport.yellowPipeWire) {
				if (!pipe.wireSet[IPipe.WireColor.Yellow.ordinal()]) {
					pipe.wireSet[IPipe.WireColor.Yellow.ordinal()] = true;
					if (!entityplayer.capabilities.isCreativeMode) {
						entityplayer.getCurrentEquippedItem().splitStack(1);
					}
					pipe.signalStrength[IPipe.WireColor.Yellow.ordinal()] = 0;
					pipe.container.scheduleNeighborChange();
					return true;
				}
			} else if (entityplayer.getCurrentEquippedItem().itemID == BuildCraftTransport.pipeGate.itemID
					|| entityplayer.getCurrentEquippedItem().itemID == BuildCraftTransport.pipeGateAutarchic.itemID)
				if (!pipe.hasInterface()) {

					pipe.gate = new GateVanilla(pipe, entityplayer.getCurrentEquippedItem());
					if (!entityplayer.capabilities.isCreativeMode) {
						entityplayer.getCurrentEquippedItem().splitStack(1);
					}
					pipe.container.scheduleRenderUpdate();
					return true;
				}

			boolean openGateGui = false;

			if (pipe.hasGate()) {
				RaytraceResult rayTraceResult = doRayTrace(world, x, y, z, entityplayer);

				if (rayTraceResult != null && rayTraceResult.hitPart == Part.Gate) {
					openGateGui = true;
				}
			}

			if (openGateGui) {
				pipe.gate.openGui(entityplayer);

				return true;
			} else
				return pipe.blockActivated(world, x, y, z, entityplayer);
		}

		return false;
	}

	private boolean stripEquipment(Pipe pipe) {

		// Try to strip wires first, starting with yellow.
		for (IPipe.WireColor color : IPipe.WireColor.values())
			if (pipe.wireSet[color.reverse().ordinal()]) {
				if (!CoreProxy.proxy.isRenderWorld(pipe.worldObj)) {
					dropWire(color.reverse(), pipe.worldObj, pipe.xCoord, pipe.yCoord, pipe.zCoord);
				}
				pipe.wireSet[color.reverse().ordinal()] = false;
				// pipe.worldObj.markBlockNeedsUpdate(pipe.xCoord, pipe.yCoord, pipe.zCoord);
				pipe.container.scheduleRenderUpdate();
				return true;
			}

		// Try to strip gate next
		if (pipe.hasGate()) {
			if (!CoreProxy.proxy.isRenderWorld(pipe.worldObj)) {
				pipe.gate.dropGate(pipe.worldObj, pipe.xCoord, pipe.yCoord, pipe.zCoord);
			}
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
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k, int l) {

		TileEntity tile = iblockaccess.getBlockTileEntity(i, j, k);
		if (!(tile instanceof IPipeRenderState))
			return null;
		return ((IPipeRenderState) tile).getRenderState().currentTexture;

		// Pipe pipe = getPipe(iblockaccess, i, j, k);
		// if (!isValid(pipe)) {
		// CoreProxy.BindTexture(DefaultProps.TEXTURE_BLOCKS);
		// return 0;
		// }
		// int pipeTexture = pipe.getPipeTexture();
		// if (pipeTexture > 255) {
		// CoreProxy.BindTexture(DefaultProps.TEXTURE_EXTERNAL);
		// return pipeTexture - 256;
		// }
		// CoreProxy.BindTexture(DefaultProps.TEXTURE_BLOCKS);
		// return pipeTexture;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int i, int j, int k, Entity entity) {
		super.onEntityCollidedWithBlock(world, i, j, k, entity);

		Pipe pipe = getPipe(world, i, j, k);

		if (isValid(pipe)) {
			pipe.onEntityCollidedWithBlock(entity);
		}
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
		Pipe pipe = getPipe(world, x, y, z);

		if (isValid(pipe))
			return pipe.canConnectRedstone();
		else
			return false;
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess iblockaccess, int x, int y, int z, int l) {
		Pipe pipe = getPipe(iblockaccess, x, y, z);

		if (isValid(pipe))
			return pipe.isPoweringTo(l);
		else
			return 0;
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int i, int j, int k, int l) {
		Pipe pipe = getPipe(world, i, j, k);

		if (isValid(pipe))
			return pipe.isIndirectlyPoweringTo(l);
		else
			return 0;
	}

	@SuppressWarnings({ "all" })
	@Override
	public void randomDisplayTick(World world, int i, int j, int k, Random random) {
		Pipe pipe = getPipe(world, i, j, k);

		if (isValid(pipe)) {
			pipe.randomDisplayTick(random);
		}
	}

	/* Registration ******************************************************** */

	public static Map<Integer, Class<? extends Pipe>> pipes = new HashMap<Integer, Class<? extends Pipe>>();

	static long lastRemovedDate = -1;
	public static Map<BlockIndex, Pipe> pipeRemoved = new HashMap<BlockIndex, Pipe>();

	public static ItemPipe registerPipe(int key, Class<? extends Pipe> clas) {
		ItemPipe item = new ItemPipe(key);
		item.setUnlocalizedName("buildcraftPipe." + clas.getSimpleName().toLowerCase(Locale.ENGLISH));
		GameRegistry.registerItem(item, item.getUnlocalizedName());

		pipes.put(item.itemID, clas);

		Pipe dummyPipe = createPipe(item.itemID);
		if (dummyPipe != null) {
			item.setPipeIconIndex(dummyPipe.getIconIndexForItem());
			TransportProxy.proxy.setIconProviderFromPipe(item, dummyPipe);
		}
		return item;
	}
	
	public static boolean isPipeRegistered(int key) {
		return pipes.containsKey(key);
	}

	public static Pipe createPipe(int key) {

		try {
			Class<? extends Pipe> pipe = pipes.get(key);
			if (pipe != null)
				return pipe.getConstructor(int.class).newInstance(key);
			else {
				BuildCraftCore.bcLog.warning("Detected pipe with unknown key (" + key + "). Did you remove a buildcraft addon?");
			}

		} catch (Throwable t) {
			t.printStackTrace();
		}

		return null;
	}

	public static boolean placePipe(Pipe pipe, World world, int i, int j, int k, int blockId, int meta) {
		if (world.isRemote)
			return true;

		boolean placed = world.setBlock(i, j, k, blockId, meta, 1);

		if (placed) {

			TileGenericPipe tile = (TileGenericPipe) world.getBlockTileEntity(i, j, k);
			tile.initialize(pipe);
		}

		return placed;
	}

	public static Pipe getPipe(IBlockAccess blockAccess, int i, int j, int k) {

		TileEntity tile = blockAccess.getBlockTileEntity(i, j, k);

		if (!(tile instanceof TileGenericPipe) || tile.isInvalid())
			return null;

		return ((TileGenericPipe) tile).pipe;
	}

	public static boolean isFullyDefined(Pipe pipe) {
		return pipe != null && pipe.transport != null && pipe.logic != null;
	}

	public static boolean isValid(Pipe pipe) {
		return isFullyDefined(pipe);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister)
	{
		if (!skippedFirstIconRegister){
			skippedFirstIconRegister = true;
			return;
		}
		BuildCraftTransport.instance.gateIconProvider.registerIcons(iconRegister);
		BuildCraftTransport.instance.wireIconProvider.registerIcons(iconRegister);
		for (int i : pipes.keySet()){
			Pipe dummyPipe = createPipe(i);
			if (dummyPipe != null){
				dummyPipe.getIconProvider().registerIcons(iconRegister);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int par1, int par2) {
		return BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.Stripes);
	}

	/**
     * Spawn a digging particle effect in the world, this is a wrapper around
     * EffectRenderer.addBlockHitEffects to allow the block more control over
     * the particles. Useful when you have entirely different texture sheets for
     * different sides/locations in the world.
     *
     * @param world The current world
     * @param target The target the player is looking at {x/y/z/side/sub}
     * @param effectRenderer A reference to the current effect renderer.
     * @return True to prevent vanilla digging particles form spawning.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public boolean addBlockHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer) {
        int x = target.blockX;
        int y = target.blockY;
        int z = target.blockZ;

        Pipe pipe = getPipe(worldObj, x, y, z);
    	if (pipe == null) return false;

    	Icon icon = pipe.getIconProvider().getIcon(pipe.getIconIndexForItem());

        int sideHit = target.sideHit;

        Block block = BuildCraftTransport.genericPipeBlock;
        float b = 0.1F;
        double px = x + rand.nextDouble() * (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - (b * 2.0F)) + b + block.getBlockBoundsMinX();
        double py = y + rand.nextDouble() * (block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() - (b * 2.0F)) + b + block.getBlockBoundsMinY();
        double pz = z + rand.nextDouble() * (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() - (b * 2.0F)) + b + block.getBlockBoundsMinZ();

        if (sideHit == 0) {
            py = (double) y + block.getBlockBoundsMinY() - (double) b;
        }

        if (sideHit == 1) {
            py = (double) y + block.getBlockBoundsMaxY() + (double) b;
        }

        if (sideHit == 2) {
            pz = (double) z + block.getBlockBoundsMinZ() - (double) b;
        }

        if (sideHit == 3) {
            pz = (double) z + block.getBlockBoundsMaxZ() + (double) b;
        }

        if (sideHit == 4) {
            px = (double) x + block.getBlockBoundsMinX() - (double) b;
        }

        if (sideHit == 5) {
            px = (double) x + block.getBlockBoundsMaxX() + (double) b;
        }

        EntityDiggingFX fx = new EntityDiggingFX(worldObj, px, py, pz, 0.0D, 0.0D, 0.0D, block, sideHit, worldObj.getBlockMetadata(x, y, z), Minecraft.getMinecraft().renderEngine);
		fx.setParticleIcon(Minecraft.getMinecraft().renderEngine, icon);
        effectRenderer.addEffect(fx.func_70596_a(x, y, z).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
        return true;
    }

    /**
     * Spawn particles for when the block is destroyed. Due to the nature of how
     * this is invoked, the x/y/z locations are not always guaranteed to host
     * your block. So be sure to do proper sanity checks before assuming that
     * the location is this block.
     *
     * @param world The current world
     * @param x X position to spawn the particle
     * @param y Y position to spawn the particle
     * @param z Z position to spawn the particle
     * @param meta The metadata for the block before it was destroyed.
     * @param effectRenderer A reference to the current effect renderer.
     * @return True to prevent vanilla break particles from spawning.
     */
    @SideOnly(Side.CLIENT)
    @Override
    public boolean addBlockDestroyEffects(World worldObj, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
    	Pipe pipe = getPipe(worldObj, x, y, z);
    	if (pipe == null) return false;

    	Icon icon = pipe.getIconProvider().getIcon(pipe.getIconIndexForItem());

        byte its = 4;
        for (int i = 0; i < its; ++i) {
            for (int j = 0; j < its; ++j) {
                for (int k = 0; k < its; ++k) {
                    double px = x + (i + 0.5D) / (double) its;
                    double py = y + (j + 0.5D) / (double) its;
                    double pz = z + (k + 0.5D) / (double) its;
                    int random = rand.nextInt(6);
                    EntityDiggingFX fx = new EntityDiggingFX(worldObj, px, py, pz, px - x - 0.5D, py - y - 0.5D, pz - z - 0.5D, BuildCraftTransport.genericPipeBlock, random, meta, Minecraft.getMinecraft().renderEngine);
					fx.setParticleIcon(Minecraft.getMinecraft().renderEngine, icon);
                    effectRenderer.addEffect(fx.func_70596_a(x, y, z));
                }
            }
        }
        return true;
    }
}
