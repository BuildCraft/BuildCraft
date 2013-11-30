/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
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
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
import buildcraft.BuildCraftTransport;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.ISolidSideTile;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.BlockIndex;
import buildcraft.core.CoreConstants;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.BCLog;
import buildcraft.core.utils.Utils;
import buildcraft.core.utils.MatrixTranformations;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Arrays;
import net.minecraft.client.Minecraft;

public class BlockGenericPipe extends BlockBuildCraft {

	static enum Part {

		Pipe,
		Gate,
		Facade,
		Plug
	}

	static class RaytraceResult {

		RaytraceResult(Part hitPart, MovingObjectPosition movingObjectPosition, AxisAlignedBB boundingBox, ForgeDirection side) {
			this.hitPart = hitPart;
			this.movingObjectPosition = movingObjectPosition;
			this.boundingBox = boundingBox;
			this.sideHit = side;
		}
		public final Part hitPart;
		public final MovingObjectPosition movingObjectPosition;
		public final AxisAlignedBB boundingBox;
		public final ForgeDirection sideHit;

		@Override
		public String toString() {
			return String.format("RayTraceResult: %s, %s", hitPart == null ? "null" : hitPart.name(), boundingBox == null ? "null" : boundingBox.toString());
		}
	}
	private static final ForgeDirection[] DIR_VALUES = ForgeDirection.values();
	private boolean skippedFirstIconRegister;
	private int renderMask = 0;

	/* Defined subprograms ************************************************* */
	public BlockGenericPipe(int i) {
		super(i, Material.glass);
		setRenderAllSides();
		setCreativeTab(null);
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

	public void setRenderMask(int mask) {
		renderMask = mask;
	}

	public final void setRenderAllSides() {
		renderMask = 0x3f;
	}

	public void setRenderSide(ForgeDirection side, boolean render) {
		if (render) {
			renderMask |= 1 << side.ordinal();
		} else {
			renderMask &= ~(1 << side.ordinal());
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side) {
		return (renderMask & (1 << side)) != 0;
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
		setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
		super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);

		TileEntity tile1 = world.getBlockTileEntity(i, j, k);
		if (tile1 instanceof TileGenericPipe) {
			TileGenericPipe tileG = (TileGenericPipe) tile1;

			if (tileG.isPipeConnected(ForgeDirection.WEST)) {
				setBlockBounds(0.0F, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.isPipeConnected(ForgeDirection.EAST)) {
				setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, 1.0F, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.isPipeConnected(ForgeDirection.DOWN)) {
				setBlockBounds(CoreConstants.PIPE_MIN_POS, 0.0F, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.isPipeConnected(ForgeDirection.UP)) {
				setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, 1.0F, CoreConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.isPipeConnected(ForgeDirection.NORTH)) {
				setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, 0.0F, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.isPipeConnected(ForgeDirection.SOUTH)) {
				setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, 1.0F);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			float facadeThickness = TransportConstants.FACADE_THICKNESS;

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

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		RaytraceResult rayTraceResult = doRayTrace(world, x, y, z, Minecraft.getMinecraft().thePlayer);

		if (rayTraceResult != null && rayTraceResult.boundingBox != null) {
			AxisAlignedBB box = rayTraceResult.boundingBox;
			switch (rayTraceResult.hitPart) {
				case Gate:
				case Plug: {
					float scale = 0.001F;
					box = box.expand(scale, scale, scale);
					break;
				}
				case Pipe: {
					float scale = 0.08F;
					box = box.expand(scale, scale, scale);
					break;
				}
			}
			return box.getOffsetBoundingBox(x, y, z);
		}
		return super.getSelectedBoundingBoxFromPool(world, x, y, z).expand(-0.85F, -0.85F, -0.85F);
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

	private RaytraceResult doRayTrace(World world, int x, int y, int z, EntityPlayer entityPlayer) {
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

	private RaytraceResult doRayTrace(World world, int x, int y, int z, Vec3 origin, Vec3 direction) {
		TileEntity pipeTileEntity = world.getBlockTileEntity(x, y, z);

		TileGenericPipe tileG = null;
		if (pipeTileEntity instanceof TileGenericPipe)
			tileG = (TileGenericPipe) pipeTileEntity;

		if (tileG == null)
			return null;

		Pipe pipe = tileG.pipe;

		if (!isValid(pipe))
			return null;

		/**
		 * pipe hits along x, y, and z axis, gate (all 6 sides) [and
		 * wires+facades]
		 */
		MovingObjectPosition[] hits = new MovingObjectPosition[25];
		AxisAlignedBB[] boxes = new AxisAlignedBB[25];
		ForgeDirection[] sideHit = new ForgeDirection[25];
		Arrays.fill(sideHit, ForgeDirection.UNKNOWN);

		// pipe

		for (ForgeDirection side : DIR_VALUES) {
			if (side == ForgeDirection.UNKNOWN || tileG.isPipeConnected(side)) {
				AxisAlignedBB bb = getPipeBoundingBox(side);
				setBlockBounds(bb);
				boxes[side.ordinal()] = bb;
				hits[side.ordinal()] = super.collisionRayTrace(world, x, y, z, origin, direction);
				sideHit[side.ordinal()] = side;
			}
		}

		// gates

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			if (pipe.hasGate(side)) {
				AxisAlignedBB bb = getGateBoundingBox(side);
				setBlockBounds(bb);
				boxes[7 + side.ordinal()] = bb;
				hits[7 + side.ordinal()] = super.collisionRayTrace(world, x, y, z, origin, direction);
				sideHit[7 + side.ordinal()] = side;
			}
		}

		// facades

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			if (tileG.hasFacade(side)) {
				AxisAlignedBB bb = getFacadeBoundingBox(side);
				setBlockBounds(bb);
				boxes[13 + side.ordinal()] = bb;
				hits[13 + side.ordinal()] = super.collisionRayTrace(world, x, y, z, origin, direction);
				sideHit[13 + side.ordinal()] = side;
			}
		}

		// plugs

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			if (tileG.hasPlug(side)) {
				AxisAlignedBB bb = getPlugBoundingBox(side);
				setBlockBounds(bb);
				boxes[19 + side.ordinal()] = bb;
				hits[19 + side.ordinal()] = super.collisionRayTrace(world, x, y, z, origin, direction);
				sideHit[19 + side.ordinal()] = side;
			}
		}

		// TODO: check wires

		// get closest hit

		double minLengthSquared = Double.POSITIVE_INFINITY;
		int minIndex = -1;

		for (int i = 0; i < hits.length; i++) {
			MovingObjectPosition hit = hits[i];
			if (hit == null)
				continue;

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

			if (minIndex < 7) {
				hitPart = Part.Pipe;
			} else if (minIndex < 13) {
				hitPart = Part.Gate;
			} else if (minIndex < 19) {
				hitPart = Part.Facade;
			} else {
				hitPart = Part.Plug;
			}

			return new RaytraceResult(hitPart, hits[minIndex], boxes[minIndex], sideHit[minIndex]);
		}
	}

	private void setBlockBounds(AxisAlignedBB bb) {
		setBlockBounds((float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ);
	}

	private AxisAlignedBB getGateBoundingBox(ForgeDirection side) {
		float min = CoreConstants.PIPE_MIN_POS + 0.05F;
		float max = CoreConstants.PIPE_MAX_POS - 0.05F;

		float[][] bounds = new float[3][2];
		// X START - END
		bounds[0][0] = min;
		bounds[0][1] = max;
		// Y START - END
		bounds[1][0] = CoreConstants.PIPE_MIN_POS - 0.10F;
		bounds[1][1] = CoreConstants.PIPE_MIN_POS;
		// Z START - END
		bounds[2][0] = min;
		bounds[2][1] = max;

		MatrixTranformations.transform(bounds, side);
		return AxisAlignedBB.getAABBPool().getAABB(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	private AxisAlignedBB getFacadeBoundingBox(ForgeDirection side) {
		float[][] bounds = new float[3][2];
		// X START - END
		bounds[0][0] = 0.0F;
		bounds[0][1] = 1.0F;
		// Y START - END
		bounds[1][0] = 0.0F;
		bounds[1][1] = TransportConstants.FACADE_THICKNESS;
		// Z START - END
		bounds[2][0] = 0.0F;
		bounds[2][1] = 1.0F;

		MatrixTranformations.transform(bounds, side);
		return AxisAlignedBB.getAABBPool().getAABB(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	private AxisAlignedBB getPlugBoundingBox(ForgeDirection side) {
		float[][] bounds = new float[3][2];
		// X START - END
		bounds[0][0] = 0.25F;
		bounds[0][1] = 0.75F;
		// Y START - END
		bounds[1][0] = 0.125F;
		bounds[1][1] = 0.251F;
		// Z START - END
		bounds[2][0] = 0.25F;
		bounds[2][1] = 0.75F;

		MatrixTranformations.transform(bounds, side);
		return AxisAlignedBB.getAABBPool().getAABB(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	private AxisAlignedBB getPipeBoundingBox(ForgeDirection side) {
		float min = CoreConstants.PIPE_MIN_POS;
		float max = CoreConstants.PIPE_MAX_POS;

		if (side == ForgeDirection.UNKNOWN) {
			return AxisAlignedBB.getAABBPool().getAABB(min, min, min, max, max, max);
		}

		float[][] bounds = new float[3][2];
		// X START - END
		bounds[0][0] = min;
		bounds[0][1] = max;
		// Y START - END
		bounds[1][0] = 0;
		bounds[1][1] = min;
		// Z START - END
		bounds[2][0] = min;
		bounds[2][1] = max;

		MatrixTranformations.transform(bounds, side);
		return AxisAlignedBB.getAABBPool().getAABB(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	public static void removePipe(Pipe pipe) {
		if (pipe == null)
			return;

		if (isValid(pipe)) {
			pipe.onBlockRemoval();
		}

		World world = pipe.container.worldObj;

		if (world == null)
			return;

		int x = pipe.container.xCoord;
		int y = pipe.container.yCoord;
		int z = pipe.container.zCoord;

		if (lastRemovedDate != world.getTotalWorldTime()) {
			lastRemovedDate = world.getTotalWorldTime();
			pipeRemoved.clear();
		}

		pipeRemoved.put(new BlockIndex(x, y, z), pipe);
		world.removeBlockTileEntity(x, y, z);
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

	@Override
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

	@SideOnly(Side.CLIENT)
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		RaytraceResult rayTraceResult = doRayTrace(world, x, y, z, Minecraft.getMinecraft().thePlayer);

		if (rayTraceResult != null && rayTraceResult.boundingBox != null) {
			switch (rayTraceResult.hitPart) {
				case Gate:
					Pipe pipe = getPipe(world, x, y, z);
					return pipe.gate.getGateItem();
				case Plug:
					return new ItemStack(BuildCraftTransport.plugItem);
			}
		}
		return super.getPickBlock(target, world, x, y, z);
	}

	/* Wrappers ************************************************************ */
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int id) {
		super.onNeighborBlockChange(world, x, y, z, id);

		Pipe pipe = getPipe(world, x, y, z);

		if (isValid(pipe)) {
			pipe.container.scheduleNeighborChange();
			pipe.container.redstonePowered = world.isBlockIndirectlyGettingPowered(x, y, z);
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
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, x, y, z, placer, stack);
		Pipe pipe = getPipe(world, x, y, z);

		if (isValid(pipe)) {
			pipe.onBlockPlacedBy(placer);
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float xOffset, float yOffset, float zOffset) {
		super.onBlockActivated(world, x, y, z, player, side, xOffset, yOffset, zOffset);

		world.notifyBlocksOfNeighborChange(x, y, z, BuildCraftTransport.genericPipeBlock.blockID);

		Pipe pipe = getPipe(world, x, y, z);

		if (isValid(pipe)) {
			ItemStack currentItem = player.getCurrentEquippedItem();

			// Right click while sneaking with empty hand to strip equipment
			// from the pipe.
			if (player.isSneaking() && currentItem == null) {
				if (stripEquipment(world, x, y, z, player, pipe))
					return true;
			} else if (currentItem == null) {
				// Fall through the end of the test
			} else if (currentItem.itemID == Item.sign.itemID)
				// Sign will be placed anyway, so lets show the sign gui
				return false;
			else if (currentItem.getItem() instanceof ItemPipe)
				return false;
			else if (currentItem.getItem() instanceof IToolWrench)
				// Only check the instance at this point. Call the IToolWrench
				// interface callbacks for the individual pipe/logic calls
				return pipe.blockActivated(player);
			else if (currentItem.getItem() == BuildCraftTransport.redPipeWire) {
				if (addOrStripWire(player, pipe, IPipe.WireColor.Red)) {
					return true;
				}
			} else if (currentItem.getItem() == BuildCraftTransport.bluePipeWire) {
				if (addOrStripWire(player, pipe, IPipe.WireColor.Blue)) {
					return true;
				}
			} else if (currentItem.getItem() == BuildCraftTransport.greenPipeWire) {
				if (addOrStripWire(player, pipe, IPipe.WireColor.Green)) {
					return true;
				}
			} else if (currentItem.getItem() == BuildCraftTransport.yellowPipeWire) {
				if (addOrStripWire(player, pipe, IPipe.WireColor.Yellow)) {
					return true;
				}
			} else if (currentItem.getItem() instanceof ItemGate) {
				if (addOrStripGate(world, x, y, z, player, pipe)) {
					return true;
				}
			} else if (currentItem.getItem() instanceof ItemPlug) {
				if (addOrStripPlug(world, x, y, z, player, ForgeDirection.getOrientation(side), pipe)) {
					return true;
				}
			} else if (currentItem.getItem() instanceof ItemFacade)
				if (addOrStripFacade(world, x, y, z, player, ForgeDirection.getOrientation(side), pipe)) {
					return true;
				}

			boolean clickedOnGate = false;

			if (pipe.hasGate()) {
				RaytraceResult rayTraceResult = doRayTrace(world, x, y, z, player);

				if (rayTraceResult != null && rayTraceResult.hitPart == Part.Gate) {
					clickedOnGate = true;
				}
			}

			if (clickedOnGate) {
				pipe.gate.openGui(player);
				return true;
			} else
				return pipe.blockActivated(player);
		}

		return false;
	}

	private boolean addOrStripGate(World world, int x, int y, int z, EntityPlayer player, Pipe pipe) {
		if (addGate(player, pipe))
			return true;
		if (player.isSneaking()) {
			RaytraceResult rayTraceResult = doRayTrace(world, x, y, z, player);
			if (rayTraceResult != null && rayTraceResult.hitPart == Part.Gate) {
				if (stripGate(pipe))
					return true;
			}
		}
		return false;
	}

	private boolean addGate(EntityPlayer player, Pipe pipe) {
		if (!pipe.hasGate()) {
			pipe.gate = Gate.makeGate(pipe, player.getCurrentEquippedItem());
			if (!player.capabilities.isCreativeMode) {
				player.getCurrentEquippedItem().splitStack(1);
			}
			pipe.container.scheduleRenderUpdate();
			return true;
		}
		return false;
	}

	private boolean stripGate(Pipe pipe) {
		if (pipe.hasGate()) {
			if (!CoreProxy.proxy.isRenderWorld(pipe.container.worldObj)) {
				pipe.gate.dropGate();
			}
			pipe.resetGate();
			return true;
		}
		return false;
	}

	private boolean addOrStripWire(EntityPlayer player, Pipe pipe, IPipe.WireColor color) {
		if (addWire(pipe, color)) {
			if (!player.capabilities.isCreativeMode) {
				player.getCurrentEquippedItem().splitStack(1);
			}
			return true;
		}
		return player.isSneaking() && stripWire(pipe, color);
	}

	private boolean addWire(Pipe pipe, IPipe.WireColor color) {
		if (!pipe.wireSet[color.ordinal()]) {
			pipe.wireSet[color.ordinal()] = true;
			pipe.signalStrength[color.ordinal()] = 0;
			pipe.container.scheduleNeighborChange();
			return true;
		}
		return false;
	}

	private boolean stripWire(Pipe pipe, IPipe.WireColor color) {
		if (pipe.wireSet[color.ordinal()]) {
			if (!CoreProxy.proxy.isRenderWorld(pipe.container.worldObj)) {
				dropWire(color, pipe);
			}
			pipe.wireSet[color.ordinal()] = false;
			pipe.container.scheduleRenderUpdate();
			return true;
		}
		return false;
	}

	private boolean addOrStripFacade(World world, int x, int y, int z, EntityPlayer player, ForgeDirection side, Pipe pipe) {
		RaytraceResult rayTraceResult = doRayTrace(world, x, y, z, player);
		if (player.isSneaking()) {
			if (rayTraceResult != null && rayTraceResult.hitPart == Part.Facade) {
				if (stripFacade(pipe, rayTraceResult.sideHit))
					return true;
			}
		}
		if (rayTraceResult != null && (rayTraceResult.hitPart != Part.Facade)) {
			if (addFacade(player, pipe, rayTraceResult.sideHit != null && rayTraceResult.sideHit != ForgeDirection.UNKNOWN ? rayTraceResult.sideHit : side))
				return true;
		}
		return false;
	}

	private boolean addFacade(EntityPlayer player, Pipe pipe, ForgeDirection side) {
		ItemStack stack = player.getCurrentEquippedItem();
		if (pipe.container.addFacade(side, ItemFacade.getBlockId(stack), ItemFacade.getMetaData(stack))) {
			if (!player.capabilities.isCreativeMode) {
				stack.stackSize--;
			}
			return true;
		}
		return false;
	}

	private boolean stripFacade(Pipe pipe, ForgeDirection side) {
		return pipe.container.dropFacade(side);
	}

	private boolean addOrStripPlug(World world, int x, int y, int z, EntityPlayer player, ForgeDirection side, Pipe pipe) {
		RaytraceResult rayTraceResult = doRayTrace(world, x, y, z, player);
		if (player.isSneaking()) {
			if (rayTraceResult != null && rayTraceResult.hitPart == Part.Plug) {
				if (stripPlug(pipe, rayTraceResult.sideHit))
					return true;
			}
		}
		if (rayTraceResult != null && (rayTraceResult.hitPart == Part.Pipe || rayTraceResult.hitPart == Part.Gate)) {
			if (addPlug(player, pipe, rayTraceResult.sideHit != null && rayTraceResult.sideHit != ForgeDirection.UNKNOWN ? rayTraceResult.sideHit : side))
				return true;
		}
		return false;
	}

	private boolean addPlug(EntityPlayer player, Pipe pipe, ForgeDirection side) {
		ItemStack stack = player.getCurrentEquippedItem();
		if (pipe.container.addPlug(side)) {
			if (!player.capabilities.isCreativeMode) {
				stack.stackSize--;
			}
			return true;
		}
		return false;
	}

	private boolean stripPlug(Pipe pipe, ForgeDirection side) {
		return pipe.container.removeAndDropPlug(side);
	}

	private boolean stripEquipment(World world, int x, int y, int z, EntityPlayer player, Pipe pipe) {
		// Try to strip facades first
		RaytraceResult rayTraceResult = doRayTrace(world, x, y, z, player);
		if (rayTraceResult != null && rayTraceResult.hitPart == Part.Facade) {
			if (stripFacade(pipe, rayTraceResult.sideHit))
				return true;
		}

		// Try to strip wires second, starting with yellow.
		for (IPipe.WireColor color : IPipe.WireColor.values()) {
			if (stripWire(pipe, color))
				return true;
		}

		return stripGate(pipe);
	}

	/**
	 * Drops a pipe wire item of the passed color.
	 *
	 * @param color
	 */
	private void dropWire(IPipe.WireColor color, Pipe pipe) {

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
		pipe.dropItem(new ItemStack(wireItem));

	}

	@SuppressWarnings({"all"})
	@SideOnly(Side.CLIENT)
	@Override
	public Icon getBlockTexture(IBlockAccess iblockaccess, int x, int y, int z, int side) {

		TileEntity tile = iblockaccess.getBlockTileEntity(x, y, z);
		if (!(tile instanceof IPipeRenderState))
			return null;
		if (((IPipeRenderState) tile).getRenderState().textureArray != null)
			return ((IPipeRenderState) tile).getRenderState().textureArray[side];
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

	@SuppressWarnings({"all"})
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
				BCLog.logger.warning("Detected pipe with unknown key (" + key + "). Did you remove a buildcraft addon?");
			}

		} catch (Throwable t) {
			BCLog.logger.warning("Failed to create pipe with (" + key + "). No valid constructor found. Possibly a item ID conflit.");
		}

		return null;
	}

	public static boolean placePipe(Pipe pipe, World world, int i, int j, int k, int blockId, int meta) {
		if (world.isRemote)
			return true;

		boolean placed = world.setBlock(i, j, k, blockId, meta, 3);

		if (placed) {
			TileGenericPipe tile = (TileGenericPipe) world.getBlockTileEntity(i, j, k);
			tile.initialize(pipe);
			tile.sendUpdateToClient();
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
		return pipe != null && pipe.transport != null;
	}

	public static boolean isValid(Pipe pipe) {
		return isFullyDefined(pipe);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {
		if (!skippedFirstIconRegister) {
			skippedFirstIconRegister = true;
			return;
		}
		BuildCraftTransport.instance.gateIconProvider.registerIcons(iconRegister);
		BuildCraftTransport.instance.wireIconProvider.registerIcons(iconRegister);
		for (int i : pipes.keySet()) {
			Pipe dummyPipe = createPipe(i);
			if (dummyPipe != null) {
				dummyPipe.getIconProvider().registerIcons(iconRegister);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int par1, int par2) {
		return BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.Stripes.ordinal());
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
		if (pipe == null)
			return false;

		Icon icon = pipe.getIconProvider().getIcon(pipe.getIconIndexForItem());

		int sideHit = target.sideHit;

		Block block = BuildCraftTransport.genericPipeBlock;
		float b = 0.1F;
		double px = x + rand.nextDouble() * (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - (b * 2.0F)) + b + block.getBlockBoundsMinX();
		double py = y + rand.nextDouble() * (block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() - (b * 2.0F)) + b + block.getBlockBoundsMinY();
		double pz = z + rand.nextDouble() * (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() - (b * 2.0F)) + b + block.getBlockBoundsMinZ();

		if (sideHit == 0) {
			py = y + block.getBlockBoundsMinY() - b;
		}

		if (sideHit == 1) {
			py = y + block.getBlockBoundsMaxY() + b;
		}

		if (sideHit == 2) {
			pz = z + block.getBlockBoundsMinZ() - b;
		}

		if (sideHit == 3) {
			pz = z + block.getBlockBoundsMaxZ() + b;
		}

		if (sideHit == 4) {
			px = x + block.getBlockBoundsMinX() - b;
		}

		if (sideHit == 5) {
			px = x + block.getBlockBoundsMaxX() + b;
		}

		EntityDiggingFX fx = new EntityDiggingFX(worldObj, px, py, pz, 0.0D, 0.0D, 0.0D, block, sideHit, worldObj.getBlockMetadata(x, y, z));
		fx.setParticleIcon(icon);
		effectRenderer.addEffect(fx.applyColourMultiplier(x, y, z).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
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
		if (pipe == null)
			return false;

		Icon icon = pipe.getIconProvider().getIcon(pipe.getIconIndexForItem());

		byte its = 4;
		for (int i = 0; i < its; ++i) {
			for (int j = 0; j < its; ++j) {
				for (int k = 0; k < its; ++k) {
					double px = x + (i + 0.5D) / its;
					double py = y + (j + 0.5D) / its;
					double pz = z + (k + 0.5D) / its;
					int random = rand.nextInt(6);
					EntityDiggingFX fx = new EntityDiggingFX(worldObj, px, py, pz, px - x - 0.5D, py - y - 0.5D, pz - z - 0.5D, BuildCraftTransport.genericPipeBlock, random, meta);
					fx.setParticleIcon(icon);
					effectRenderer.addEffect(fx.applyColourMultiplier(x, y, z));
				}
			}
		}
		return true;
	}
	public static int facadeRenderColor = -1;

	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		if (facadeRenderColor != -1) {
			return facadeRenderColor;
		}
		return super.colorMultiplier(world, x, y, z);
	}
}
