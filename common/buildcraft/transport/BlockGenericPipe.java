/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.BCLog;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.api.events.PipePlacedEvent;
import buildcraft.api.events.RobotPlacementEvent;
import buildcraft.api.gates.GateExpansions;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.PipeWire;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.CoreConstants;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.ItemMapLocation;
import buildcraft.core.ItemRobot;
import buildcraft.core.TileBuffer;
import buildcraft.core.robots.DockingStation;
import buildcraft.core.robots.EntityRobot;
import buildcraft.core.utils.IModelRegister;
import buildcraft.core.utils.MatrixTranformations;
import buildcraft.core.utils.Utils;
import buildcraft.transport.gates.GateDefinition;
import buildcraft.transport.gates.GateFactory;
import buildcraft.transport.gates.ItemGate;
import buildcraft.transport.utils.FacadeMatrix;


public class BlockGenericPipe extends BlockBuildCraft implements IModelRegister {

	public static int facadeRenderColor = -1;
	public static Map<Item, Class<? extends Pipe>> pipes = new HashMap<Item, Class<? extends Pipe>>();
	public static Map<BlockPos, Pipe<?>> pipeRemoved = new HashMap<BlockPos, Pipe<?>>();

	private static long lastRemovedDate = -1;

	private static final EnumFacing[] DIR_VALUES = EnumFacing.values();

	static enum Part {
		Pipe,
		Gate,
		Facade,
		Plug,
		RobotStation
	}

	static class RaytraceResult {
		public final Part hitPart;
		public final MovingObjectPosition movingObjectPosition;
		public final AxisAlignedBB boundingBox;
		public final EnumFacing sideHit;

		RaytraceResult(Part hitPart, MovingObjectPosition movingObjectPosition, AxisAlignedBB boundingBox, EnumFacing side) {
			this.hitPart = hitPart;
			this.movingObjectPosition = movingObjectPosition;
			this.boundingBox = boundingBox;
			this.sideHit = side;
		}

		@Override
		public String toString() {
			return String.format("RayTraceResult: %s, %s", hitPart == null ? "null" : hitPart.name(), boundingBox == null ? "null" : boundingBox.toString());
		}
	}

	private boolean skippedFirstIconRegister;

	/* Defined subprograms ************************************************* */
	public BlockGenericPipe() {
		super(Material.glass, new PropertyEnum[]{FACING_6_PROP});
		setCreativeTab(null);
	}

	@Override
	protected BlockState createBlockState() {
		return new ExtendedBlockState(this, new PropertyEnum[]{FACING_6_PROP}, new IUnlistedProperty[]{TileGenericPipe.CORE_STATE_PROP, TileGenericPipe.RENDER_STATE_PROP});
	}

	@Override
	public float getBlockHardness(World worldIn, BlockPos pos) {
		return BuildCraftTransport.pipeDurability;
	}

	/*@Override
	public boolean canRenderInPass(int pass) {
		PipeRendererWorld.renderPass = pass;
		return true;
	}

	@Override
	public int getRenderBlockPass() {
		return 1;
	}*/

	@Override
	public boolean isOpaqueCube() {
		return false;
	}


	@Override
	public boolean canBeReplacedByLeaves(IBlockAccess world, BlockPos pos){
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side){
		TileEntity tile = world.getTileEntity(pos);

		if (tile instanceof ISolidSideTile) {
			return ((ISolidSideTile) tile).isSolidOnSide(side);
		}

		return false;
	}

	@Override
	public boolean isNormalCube() {
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB axisalignedbb, List list, Entity collidingEntity) {
		setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
		super.addCollisionBoxesToList(world, pos, state, axisalignedbb, list, collidingEntity);

		TileEntity tile1 = world.getTileEntity(pos);
		if (tile1 instanceof TileGenericPipe) {
			TileGenericPipe tileG = (TileGenericPipe) tile1;

			if (tileG.isPipeConnected(EnumFacing.WEST)) {
				setBlockBounds(0.0F, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, pos, state, axisalignedbb, list, collidingEntity);
			}

			if (tileG.isPipeConnected(EnumFacing.EAST)) {
				setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, 1.0F, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, pos, state, axisalignedbb, list, collidingEntity);
			}

			if (tileG.isPipeConnected(EnumFacing.DOWN)) {
				setBlockBounds(CoreConstants.PIPE_MIN_POS, 0.0F, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, pos, state, axisalignedbb, list, collidingEntity);
			}

			if (tileG.isPipeConnected(EnumFacing.UP)) {
				setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, 1.0F, CoreConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, pos, state, axisalignedbb, list, collidingEntity);
			}

			if (tileG.isPipeConnected(EnumFacing.NORTH)) {
				setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, 0.0F, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, pos, state, axisalignedbb, list, collidingEntity);
			}

			if (tileG.isPipeConnected(EnumFacing.SOUTH)) {
				setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, 1.0F);
				super.addCollisionBoxesToList(world, pos, state, axisalignedbb, list, collidingEntity);
			}

			float facadeThickness = TransportConstants.FACADE_THICKNESS;

			if (tileG.hasEnabledFacade(EnumFacing.EAST)) {
				setBlockBounds(1 - facadeThickness, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, pos, state, axisalignedbb, list, collidingEntity);
			}

			if (tileG.hasEnabledFacade(EnumFacing.WEST)) {
				setBlockBounds(0.0F, 0.0F, 0.0F, facadeThickness, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, pos, state, axisalignedbb, list, collidingEntity);
			}

			if (tileG.hasEnabledFacade(EnumFacing.UP)) {
				setBlockBounds(0.0F, 1 - facadeThickness, 0.0F, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, pos, state, axisalignedbb, list, collidingEntity);
			}

			if (tileG.hasEnabledFacade(EnumFacing.DOWN)) {
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, facadeThickness, 1.0F);
				super.addCollisionBoxesToList(world, pos, state, axisalignedbb, list, collidingEntity);
			}

			if (tileG.hasEnabledFacade(EnumFacing.SOUTH)) {
				setBlockBounds(0.0F, 0.0F, 1 - facadeThickness, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, pos, state, axisalignedbb, list, collidingEntity);
			}

			if (tileG.hasEnabledFacade(EnumFacing.NORTH)) {
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, facadeThickness);
				super.addCollisionBoxesToList(world, pos, state, axisalignedbb, list, collidingEntity);
			}
		}
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos) {
		RaytraceResult rayTraceResult = doRayTrace(world, pos, Minecraft.getMinecraft().thePlayer);

		if (rayTraceResult != null && rayTraceResult.boundingBox != null) {
			AxisAlignedBB box = rayTraceResult.boundingBox;
			switch (rayTraceResult.hitPart) {
			case Gate:
			case Plug:
			case RobotStation: {
				float scale = 0.001F;
				box = box.expand(scale, scale, scale);
				break;
			}
			case Pipe: {
				float scale = 0.08F;
				box = box.expand(scale, scale, scale);
				break;
			}
			case Facade:
				break;
			}
			return box.offset(pos.getX(), pos.getY(), pos.getZ());
		}
		return super.getSelectedBoundingBox(world, pos).expand(-0.85F, -0.85F, -0.85F);
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 origin, Vec3 direction) {
		RaytraceResult raytraceResult = doRayTrace(world, pos, origin, direction);

		if (raytraceResult == null) {
			return null;
		} else {
			return raytraceResult.movingObjectPosition;
		}
	}

	public RaytraceResult doRayTrace(World world, BlockPos pos, EntityPlayer player) {
		double reachDistance = 5;

		if (player instanceof EntityPlayerMP) {
			reachDistance = ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance();
		}

		double eyeHeight = world.isRemote ? player.getEyeHeight() - player.getDefaultEyeHeight() : player.getEyeHeight();
		Vec3 lookVec = player.getLookVec();
		Vec3 origin = new Vec3(player.posX, player.posY + eyeHeight, player.posZ);
		Vec3 direction = origin.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

		return doRayTrace(world, pos, origin, direction);
	}

	private RaytraceResult doRayTrace(World world, BlockPos pos, Vec3 origin, Vec3 direction) {
		TileEntity pipeTileEntity = world.getTileEntity(pos);

		TileGenericPipe tileG = null;
		if (pipeTileEntity instanceof TileGenericPipe) {
			tileG = (TileGenericPipe) pipeTileEntity;
		}

		if (tileG == null) {
			return null;
		}

		Pipe<?> pipe = tileG.pipe;

		if (!isValid(pipe)) {
			return null;
		}

		/**
		 * pipe hits along x, y, and z axis, gate (all 6 sides) [and
		 * wires+facades]
		 */
		MovingObjectPosition[] hits = new MovingObjectPosition[31];
		AxisAlignedBB[] boxes = new AxisAlignedBB[31];
		EnumFacing[] sideHit = new EnumFacing[31];
		Arrays.fill(sideHit, null);

		// pipe

		for (EnumFacing side : DIR_VALUES) {
			if (side == null || tileG.isPipeConnected(side)) {
				AxisAlignedBB bb = getPipeBoundingBox(side);
				setBlockBounds(bb);
				boxes[side.ordinal()] = bb;
				hits[side.ordinal()] = super.collisionRayTrace(world, pos, origin, direction);
				sideHit[side.ordinal()] = side;
			}
		}

		// gates

		for (EnumFacing side : EnumFacing.values()) {
			if (pipe.hasGate(side)) {
				AxisAlignedBB bb = getGateBoundingBox(side);
				setBlockBounds(bb);
				boxes[7 + side.ordinal()] = bb;
				hits[7 + side.ordinal()] = super.collisionRayTrace(world, pos, origin, direction);
				sideHit[7 + side.ordinal()] = side;
			}
		}

		// facades

		for (EnumFacing side : EnumFacing.values()) {
			if (tileG.hasFacade(side)) {
				AxisAlignedBB bb = getFacadeBoundingBox(side);
				setBlockBounds(bb);
				boxes[13 + side.ordinal()] = bb;
				hits[13 + side.ordinal()] = super.collisionRayTrace(world, pos, origin, direction);
				sideHit[13 + side.ordinal()] = side;
			}
		}

		// plugs

		for (EnumFacing side : EnumFacing.values()) {
			if (tileG.hasPlug(side)) {
				AxisAlignedBB bb = getPlugBoundingBox(side);
				setBlockBounds(bb);
				boxes[19 + side.ordinal()] = bb;
				hits[19 + side.ordinal()] = super.collisionRayTrace(world, pos, origin, direction);
				sideHit[19 + side.ordinal()] = side;
			}
		}

		// robotStations

		for (EnumFacing side : EnumFacing.values()) {
			if (tileG.hasRobotStation(side)) {
				AxisAlignedBB bb = getRobotStationBoundingBox(side);
				setBlockBounds(bb);
				boxes[25 + side.ordinal()] = bb;
				hits[25 + side.ordinal()] = super.collisionRayTrace(world, pos, origin, direction);
				sideHit[25 + side.ordinal()] = side;
			}
		}

		// TODO: check wires

		// get closest hit

		double minLengthSquared = Double.POSITIVE_INFINITY;
		int minIndex = -1;

		for (int i = 0; i < hits.length; i++) {
			MovingObjectPosition hit = hits[i];
			if (hit == null) {
				continue;
			}

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
			} else if (minIndex < 25) {
				hitPart = Part.Plug;
			} else {
				hitPart = Part.RobotStation;
			}

			return new RaytraceResult(hitPart, hits[minIndex], boxes[minIndex], sideHit[minIndex]);
		}
	}

	private void setBlockBounds(AxisAlignedBB bb) {
		setBlockBounds((float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ);
	}

	private AxisAlignedBB getGateBoundingBox(EnumFacing side) {
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
		return AxisAlignedBB.fromBounds(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	private AxisAlignedBB getFacadeBoundingBox(EnumFacing side) {
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
		return AxisAlignedBB.fromBounds(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	private AxisAlignedBB getPlugBoundingBox(EnumFacing side) {
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
		return AxisAlignedBB.fromBounds(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	private AxisAlignedBB getRobotStationBoundingBox(EnumFacing side) {
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
		return AxisAlignedBB.fromBounds(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	private AxisAlignedBB getPipeBoundingBox(EnumFacing side) {
		float min = CoreConstants.PIPE_MIN_POS;
		float max = CoreConstants.PIPE_MAX_POS;

		if (side == null) {
			return AxisAlignedBB.fromBounds(min, min, min, max, max, max);
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
		return AxisAlignedBB.fromBounds(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	public static void removePipe(Pipe<?> pipe) {
		if (!isValid(pipe)) {
			return;
		}

		World world = pipe.container.getWorld();

		if (world == null) {
			return;
		}

		BlockPos pos = pipe.container.getPos();

		if (lastRemovedDate != world.getTotalWorldTime()) {
			lastRemovedDate = world.getTotalWorldTime();
			pipeRemoved.clear();
		}

		pipeRemoved.put(pos, pipe);
		world.removeTileEntity(pos);

		updateNeighbourSignalState(pipe);

	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		Utils.preDestroyBlock(world, pos, state);
		removePipe(getPipe(world, pos));
		super.breakBlock(world, pos, state);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		if(!(world instanceof World)) return null;
		if (((World)world).isRemote) {
			return null;
		}

		List<ItemStack> list = new ArrayList<ItemStack>();
		Pipe<?> pipe = getPipe(world, pos);

		if (pipe == null) {
			pipe = pipeRemoved.get(pos);
		}
		
		if (pipe != null) {
			if (pipe.item != null) {
				list.add(new ItemStack(pipe.item, 1, pipe.container.getItemMetadata()));
				list.addAll(pipe.computeItemDrop());
				list.addAll(pipe.getDroppedItems());
			}
		}
		return list;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileGenericPipe();
	}

	@Override
	public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float f, int dmg) {
		if (world.isRemote) {
			return;
		}
		Pipe<?> pipe = getPipe(world, pos);

		if (pipe == null) {
			pipe = pipeRemoved.get(pos);
		}

		if (pipe != null) {
			Item k1 = pipe.item;

			if (k1 != null) {
				pipe.dropContents();
				for (ItemStack is: pipe.computeItemDrop()) {
					dropItemStack(world, pos, is);
				}
				dropItemStack(world, pos, new ItemStack(k1, 1, pipe.container.getItemMetadata()));
			}
		}
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		// Returns null to be safe - the id does not depend on the meta
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos) {
		RaytraceResult rayTraceResult = doRayTrace(world, pos, Minecraft.getMinecraft().thePlayer);

		if (rayTraceResult != null && rayTraceResult.boundingBox != null) {
			switch (rayTraceResult.hitPart) {
			case Gate:
					Pipe<?> pipe = getPipe(world, pos);
					Gate gate = pipe.gates[rayTraceResult.sideHit.ordinal()];
					return gate != null ? gate.getGateItem() : null;
			case Plug:
					return new ItemStack(BuildCraftTransport.plugItem);
			case RobotStation:
				return new ItemStack(BuildCraftTransport.robotStationItem);
			case Pipe:
				return new ItemStack(getPipe(world, pos).item, 1, getPipe(world, pos).container.getItemMetadata());
			case Facade:
				EnumFacing dir = target.sideHit;
				FacadeMatrix matrix = getPipe(world, pos).container.renderState.facadeMatrix;
				Block block = matrix.getFacadeBlock(dir);
				if (block != null) {
					return BuildCraftTransport.facadeItem.getFacadeForBlock(block,
							matrix.getFacadeMetaId(dir));
				}
			}
		}
		return null;
	}

	/* Wrappers ************************************************************ */
	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
		super.onNeighborBlockChange(world, pos, state, neighborBlock);

		Pipe<?> pipe = getPipe(world, pos);

		if (isValid(pipe)) {
			pipe.container.scheduleNeighborChange();
			pipe.container.redstoneInput = 0;
			
			for (int i = 0; i < EnumFacing.values().length; i++) {
				EnumFacing d = EnumFacing.getFront(i);
				pipe.container.redstoneInputSide[i] = getRedstoneInputToPipe(world, pos, d);
				if (pipe.container.redstoneInput < pipe.container.redstoneInputSide[i]) {
					pipe.container.redstoneInput = pipe.container.redstoneInputSide[i];
				}
			}
		}
	}
	
	private int getRedstoneInputToPipe(World world, BlockPos pos, EnumFacing d) {
		
		int input = world.getStrongPower(pos.offset(d), d);
		if (input == 0) {
			input = world.getRedstonePower(pos.offset(d), d);
			if (input == 0 && d != EnumFacing.DOWN) {
				IBlockState blockState = world.getBlockState(pos.offset(d));
				Block block = blockState.getBlock();
				if (block instanceof BlockRedstoneWire) {
					return ((EnumFacing)blockState.getValue(BlockRedstoneWire.POWER)).getIndex();
				}
			}
		}
		return input;
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		Pipe<?> pipe = getPipe(world, pos);

		System.out.println("PLACED");

		if (isValid(pipe)) {
			pipe.onBlockPlaced();
		}

		return super.onBlockPlaced(world, pos, facing, hitX, hitY, hitZ, meta, placer);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, placer, stack);
		Pipe<?> pipe = getPipe(world, pos);

		if (isValid(pipe)) {
			pipe.onBlockPlacedBy(placer);
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		super.onBlockActivated(world, pos, state, player, side, hitX, hitY, hitZ);
		BlockInteractionEvent event = new BlockInteractionEvent(player, pos, state);
		FMLCommonHandler.instance().bus().post(event);
		if (event.isCanceled()) {
			return true;
		}

		world.notifyBlockOfStateChange(pos, BuildCraftTransport.genericPipeBlock);

		Pipe<?> pipe = getPipe(world, pos);

		if (isValid(pipe)) {
			ItemStack currentItem = player.getCurrentEquippedItem();

			// Right click while sneaking with empty hand to strip equipment
			// from the pipe.
			if (player.isSneaking() && currentItem == null) {
				if (stripEquipment(world, pos, player, pipe)) {
					return true;
				}
			} else if (currentItem == null) {
				// Fall through the end of the test
			} else if (currentItem.getItem() == Items.sign) {
				// Sign will be placed anyway, so lets show the sign gui
				return false;
			} else if (currentItem.getItem() instanceof ItemPipe) {
				return false;
			} else if (currentItem.getItem() instanceof ItemGateCopier) {
				return false;
			} else if (currentItem.getItem() instanceof IToolWrench) {
				// Only check the instance at this point. Call the IToolWrench
				// interface callbacks for the individual pipe/logic calls
				return pipe.blockActivated(player);
			} else if (currentItem.getItem() instanceof ItemMapLocation) {
				// We want to be able to record pipe locations
				return false;
			} else if (PipeWire.RED.isPipeWire(currentItem)) {
				if (addOrStripWire(player, pipe, PipeWire.RED)) {
					return true;
				}
			} else if (PipeWire.BLUE.isPipeWire(currentItem)) {
				if (addOrStripWire(player, pipe, PipeWire.BLUE)) {
					return true;
				}
			} else if (PipeWire.GREEN.isPipeWire(currentItem)) {
				if (addOrStripWire(player, pipe, PipeWire.GREEN)) {
					return true;
				}
			} else if (PipeWire.YELLOW.isPipeWire(currentItem)) {
				if (addOrStripWire(player, pipe, PipeWire.YELLOW)) {
					return true;
				}
			} else if (currentItem.getItem() == Items.water_bucket) {
				if (!world.isRemote) {
					pipe.container.setColor(-1);
				}
				return true;
			} else if (currentItem.getItem() instanceof ItemGate) {
				if (addOrStripGate(world, pos, player, side, pipe)) {
					return true;
				}
			} else if (currentItem.getItem() instanceof ItemPlug) {
				if (addOrStripPlug(world, pos, player, side, pipe)) {
					return true;
				}
			} else if (currentItem.getItem() instanceof ItemRobotStation) {
				if (addOrStripRobotStation(world, pos, player, side, pipe)) {
					return true;
				}
			} else if (currentItem.getItem() instanceof ItemFacade) {
				if (addOrStripFacade(world, pos, player, side, pipe)) {
					return true;
				}
			} else if (currentItem.getItem () instanceof ItemRobot) {
				if (!world.isRemote) {
					RaytraceResult rayTraceResult = doRayTrace(world, pos,
							player);

					if (rayTraceResult != null && rayTraceResult.hitPart == Part.RobotStation) {
						DockingStation station = pipe.container.getStation(rayTraceResult.sideHit);

						if (!station.isTaken()) {
							if (ItemRobot.getRobotNBT(currentItem) == null) {
								return true;
							}
							RobotPlacementEvent robotEvent = new RobotPlacementEvent(player, ((NBTTagCompound) currentItem.getTagCompound().getTag("board")).getString("id"));
							FMLCommonHandler.instance().bus().post(robotEvent);
							if (robotEvent.isCanceled()) {
								return true;
							}
							EntityRobot robot = ((ItemRobot) currentItem.getItem())
									.createRobot(currentItem, world);
							
							if (robot != null && robot.getRegistry() != null) {
								robot.setUniqueRobotId(robot.getRegistry().getNextRobotId());
								robot.getBattery().setEnergy(EntityRobotBase.MAX_ENERGY);
	
								float px = pos.getX() + 0.5F + rayTraceResult.sideHit.getFrontOffsetX() * 0.5F;
								float py = pos.getY() + 0.5F + rayTraceResult.sideHit.getFrontOffsetY() * 0.5F;
								float pz = pos.getZ() + 0.5F + rayTraceResult.sideHit.getFrontOffsetZ() * 0.5F;
	
								robot.setPosition(px, py, pz);
								station.takeAsMain(robot);
								robot.dock(robot.getLinkedStation());
								world.spawnEntityInWorld(robot);
	
								if (!player.capabilities.isCreativeMode) {
									player.getCurrentEquippedItem().stackSize--;
								}
							}
						}

						return true;
					}
				}
			}

			Gate clickedGate = null;

			RaytraceResult rayTraceResult = doRayTrace(world, pos, player);

			if (rayTraceResult != null && rayTraceResult.hitPart == Part.Gate) {
				clickedGate = pipe.gates[rayTraceResult.sideHit.ordinal()];
			}

			if (clickedGate != null) {
				clickedGate.openGui(player);
				return true;
			} else {
				return pipe.blockActivated(player);
			}
		}

		return false;
	}

	private boolean addOrStripGate(World world, BlockPos pos, EntityPlayer player, EnumFacing side, Pipe<?> pipe) {
		RaytraceResult rayTraceResult = doRayTrace(world, pos, player);
		if (player.isSneaking()) {
			if (rayTraceResult != null && rayTraceResult.hitPart == Part.Gate) {
				if (pipe.container.hasGate(rayTraceResult.sideHit)) {
					return pipe.container.dropSideItems(rayTraceResult.sideHit);
				}
			}
		}
		if (rayTraceResult != null && rayTraceResult.hitPart == Part.Pipe) {
			if (!pipe.hasGate(side) && addGate(player, pipe, rayTraceResult.sideHit != null && rayTraceResult.sideHit != null ? rayTraceResult.sideHit : side)) {
				return true;
			}
		}
		return false;
	}

	private boolean addGate(EntityPlayer player, Pipe<?> pipe, EnumFacing side) {
		ItemStack stack = player.getCurrentEquippedItem();
		if (stack != null && stack.getItem() instanceof ItemGate && pipe.container.addGate(side, GateFactory.makeGate(pipe, stack, side))) {
			if (!player.capabilities.isCreativeMode) {
				stack.stackSize--;
			}
			return true;
		}
		return false;
	}

	private boolean addOrStripWire(EntityPlayer player, Pipe<?> pipe, PipeWire color) {
		if (addWire(pipe, color)) {
			if (!player.capabilities.isCreativeMode) {
				player.getCurrentEquippedItem().splitStack(1);
			}
			return true;
		}
		return player.isSneaking() && stripWire(pipe, color);
	}

	private boolean addWire(Pipe<?> pipe, PipeWire color) {
		if (!pipe.wireSet[color.ordinal()]) {
			pipe.wireSet[color.ordinal()] = true;
			pipe.signalStrength[color.ordinal()] = 0;

			pipe.updateSignalState();
			pipe.container.scheduleRenderUpdate();
			return true;
		}
		return false;
	}

	private boolean stripWire(Pipe<?> pipe, PipeWire color) {
		if (pipe.wireSet[color.ordinal()]) {
			if (!pipe.container.getWorld().isRemote) {
				dropWire(color, pipe);
			}

			pipe.signalStrength[color.ordinal()] = 0;
			pipe.wireSet[color.ordinal()] = false;

			pipe.updateSignalState();

			updateNeighbourSignalState(pipe);

			if (isFullyDefined(pipe)) {
				pipe.resolveActions();
			}

			pipe.container.scheduleRenderUpdate();

			return true;
		}
		return false;
	}

	private boolean addOrStripFacade(World world, BlockPos pos, EntityPlayer player, EnumFacing side, Pipe<?> pipe) {
		RaytraceResult rayTraceResult = doRayTrace(world, pos, player);
		if (player.isSneaking()) {
			if (rayTraceResult != null && rayTraceResult.hitPart == Part.Facade) {
				if (pipe.container.hasFacade(rayTraceResult.sideHit)) {
					return pipe.container.dropSideItems(rayTraceResult.sideHit);
				}
			}
		}
		if (rayTraceResult != null && rayTraceResult.hitPart == Part.Pipe) {
			if (addFacade(player, pipe, rayTraceResult.sideHit != null && rayTraceResult.sideHit != null ? rayTraceResult.sideHit : side)) {
				return true;
			}
		}
		return false;
	}

	private boolean addFacade(EntityPlayer player, Pipe<?> pipe, EnumFacing side) {
		ItemStack stack = player.getCurrentEquippedItem();
		if (stack != null && stack.getItem() instanceof ItemFacade && pipe.container.addFacade(side, ItemFacade.getFacadeStates(stack))) {
			if (!player.capabilities.isCreativeMode) {
				stack.stackSize--;
			}
			return true;
		}
		return false;
	}

	private boolean addOrStripPlug(World world, BlockPos pos, EntityPlayer player, EnumFacing side, Pipe<?> pipe) {
		RaytraceResult rayTraceResult = doRayTrace(world, pos, player);
		if (player.isSneaking()) {
			if (rayTraceResult != null && rayTraceResult.hitPart == Part.Plug) {
				if (pipe.container.dropSideItems(rayTraceResult.sideHit)) {
					return true;
				}
			}
		}
		if (rayTraceResult != null && rayTraceResult.hitPart == Part.Pipe) {
			if (addPlug(player, pipe, rayTraceResult.sideHit != null && rayTraceResult.sideHit != null ? rayTraceResult.sideHit : side)) {
				return true;
			}
		}
		return false;
	}

	private boolean addOrStripRobotStation(World world, BlockPos pos, EntityPlayer player, EnumFacing side, Pipe<?> pipe) {
		RaytraceResult rayTraceResult = doRayTrace(world, pos, player);
		if (player.isSneaking()) {
			if (rayTraceResult != null && rayTraceResult.hitPart == Part.RobotStation) {
				if (pipe.container.dropSideItems(rayTraceResult.sideHit)) {
					return true;
				}
			}
		}
		if (rayTraceResult != null && rayTraceResult.hitPart == Part.Pipe) {
			if (addRobotStation(player, pipe, rayTraceResult.sideHit != null && rayTraceResult.sideHit != null ? rayTraceResult.sideHit : side)) {
				return true;
			}
		}
		return false;
	}

	private boolean addPlug(EntityPlayer player, Pipe<?> pipe, EnumFacing side) {
		ItemStack stack = player.getCurrentEquippedItem();
		if (pipe.container.addPlug(side)) {
			if (!player.capabilities.isCreativeMode) {
				stack.stackSize--;
			}
			return true;
		}
		return false;
	}

	private boolean addRobotStation(EntityPlayer player, Pipe<?> pipe, EnumFacing side) {
		ItemStack stack = player.getCurrentEquippedItem();
		if (pipe.container.addRobotStation(side)) {
			if (!player.capabilities.isCreativeMode) {
				stack.stackSize--;
			}
			return true;
		}
		return false;
	}

	private boolean stripEquipment(World world, BlockPos pos, EntityPlayer player, Pipe<?> pipe) {
		// Try to strip facades first
		RaytraceResult rayTraceResult = doRayTrace(world, pos, player);
		if (rayTraceResult != null && rayTraceResult.hitPart != Part.Pipe) {
			if (pipe.container.dropSideItems(rayTraceResult.sideHit)) {
				return true;
			}
		}

		// Try to strip wires second, starting with yellow.
		for (PipeWire color : PipeWire.values()) {
			if (stripWire(pipe, color)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Drops a pipe wire item of the passed color.
	 *
	 * @param pipeWire
	 */
	private void dropWire(PipeWire pipeWire, Pipe<?> pipe) {
		pipe.dropItem(pipeWire.getStack());
	}


	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entity) {
		super.onEntityCollidedWithBlock(world, pos, entity);

		Pipe<?> pipe = getPipe(world, pos);

		if (isValid(pipe)) {
			pipe.onEntityCollidedWithBlock(entity);
		}
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {
		Pipe<?> pipe = getPipe(world, pos);

		if (isValid(pipe)) {
			return pipe.canConnectRedstone();
		} else {
			return false;
		}
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess worldIn, BlockPos pos, IBlockState state, EnumFacing side) {
		Pipe<?> pipe = getPipe(worldIn, pos);

		if (isValid(pipe)) {
			return pipe.isPoweringTo(side.getIndex());
		} else {
			return 0;
		}
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
		Pipe<?> pipe = getPipe(world, pos);

		if (isValid(pipe)) {
			return pipe.isIndirectlyPoweringTo(side.getIndex());
		} else {
			return 0;
		}
	}

	@SuppressWarnings({"all"})
	@Override
	@SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand) {
		Pipe pipe = getPipe(world, pos);

		if (isValid(pipe)) {
			pipe.randomDisplayTick(rand);
		}
	}

	/* Registration ******************************************************** */
	public static ItemPipe registerPipe(Class<? extends Pipe> clas, CreativeTabBuildCraft creativeTab) {
		ItemPipe item = new ItemPipe(creativeTab);
		item.setUnlocalizedName("buildcraftPipe." + clas.getSimpleName().toLowerCase(Locale.ENGLISH));
		GameRegistry.registerItem(item, item.getUnlocalizedName());

		pipes.put(item, clas);

		Pipe<?> dummyPipe = createPipe(item);
		if (dummyPipe != null) {
			item.setPipeIconIndex(dummyPipe.getIconIndexForItem());
			TransportProxy.proxy.setIconProviderFromPipe(item, dummyPipe);
		}

		return item;
	}

	public static Pipe<?> createPipe(Item key) {

		try {
			Class<? extends Pipe> pipe = pipes.get(key);
			if (pipe != null) {
				return pipe.getConstructor(Item.class).newInstance(key);
			} else {
				BCLog.logger.warn("Detected pipe with unknown key (" + key + "). Did you remove a buildcraft addon?");
			}

		} catch (Throwable t) {
			t.printStackTrace();
			BCLog.logger.warn("Failed to create pipe with (" + key + "). No valid constructor found. Possibly a item ID conflit.");
		}

		return null;
	}

	public static boolean placePipe(Pipe<?> pipe, World world, BlockPos pos, Block block, int meta, EntityPlayer player) {
		if (world.isRemote) {
			return true;
		}

		boolean placed = world.setBlockState(pos, BuildCraftTransport.genericPipeBlock.getDefaultState().withProperty(FACING_6_PROP, EnumFacing.getFront(meta)));

		if (placed) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileGenericPipe) {
				TileGenericPipe tilePipe = (TileGenericPipe) tile;
				tilePipe.initialize(pipe);
				tilePipe.sendUpdateToClient();
				FMLCommonHandler.instance().bus().post(new PipePlacedEvent(player, pipe.item.getUnlocalizedName(), pos));
			}
		}

		return placed;
	}

	public static Pipe<?> getPipe(IBlockAccess blockAccess, BlockPos pos) {
		TileEntity tile = blockAccess.getTileEntity(pos);

		if (!(tile instanceof TileGenericPipe) || tile.isInvalid()) {
			return null;
		} else {
			return ((TileGenericPipe) tile).pipe;
		}
	}

	public static boolean isFullyDefined(Pipe<?> pipe) {
		return pipe != null && pipe.transport != null && pipe.container != null;
	}

	public static boolean isValid(Pipe<?> pipe) {
		return isFullyDefined(pipe);
	}

	/*@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		if (!skippedFirstIconRegister) {
			skippedFirstIconRegister = true;
			return;
		}

		BuildCraftTransport.instance.wireIconProvider.registerIcons(iconRegister);

		for (Item i : pipes.keySet()) {
			Pipe<?> dummyPipe = createPipe(i);
			if (dummyPipe != null) {
				dummyPipe.getIconProvider().registerIcons(iconRegister);
			}
		}

		for (GateDefinition.GateMaterial material : GateDefinition.GateMaterial.VALUES) {
			material.registerBlockIcon(iconRegister);
		}

		for (GateDefinition.GateLogic logic : GateDefinition.GateLogic.VALUES) {
			logic.registerBlockIcon(iconRegister);
		}

		for (IGateExpansion expansion : GateExpansions.getExpansions()) {
			expansion.registerBlockOverlay(iconRegister);
		}
	}*/

	/**
	 * Spawn a digging particle effect in the world, this is a wrapper around
	 * EffectRenderer.addBlockHitEffects to allow the block more control over
	 * the particles. Useful when you have entirely different texture sheets for
	 * different sides/locations in the world.
	 *
	 * @param worldObj The current world
	 * @param target The target the player is looking at {x/y/z/side/sub}
	 * @param effectRenderer A reference to the current effect renderer.
	 * @return True to prevent vanilla digging particles form spawning.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer) {
		BlockPos targetPos = new BlockPos(target.hitVec);
		int x = targetPos.getX();
		int y = targetPos.getY();
		int z = targetPos.getZ();

		Pipe<?> pipe = getPipe(worldObj, targetPos);
		if (pipe == null) {
			return false;
		}

		//IIcon icon = pipe.getIconProvider().getIcon(pipe.getIconIndexForItem());

		int sideHit = target.subHit;

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

		/*EntityDiggingFX fx = new EntityDiggingFX(worldObj, px, py, pz, 0.0D, 0.0D, 0.0D, block, sideHit, worldObj.getBlockMetadata(x, y, z));
		fx.setParticleIcon(icon);
		effectRenderer.addEffect(fx.applyColourMultiplier(x, y, z).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));*/
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean addDestroyEffects(World world, BlockPos pos, net.minecraft.client.particle.EffectRenderer effectRenderer) {
		Pipe<?> pipe = getPipe(world, pos);
		if (pipe == null) {
			return false;
		}

		//IIcon icon = pipe.getIconProvider().getIcon(pipe.getIconIndexForItem());

		byte its = 4;
		for (int i = 0; i < its; ++i) {
			for (int j = 0; j < its; ++j) {
				for (int k = 0; k < its; ++k) {
					double px = pos.getX() + (i + 0.5D) / its;
					double py = pos.getY() + (j + 0.5D) / its;
					double pz = pos.getZ() + (k + 0.5D) / its;
					int random = rand.nextInt(6);
					/*EntityDiggingFX fx = new EntityDiggingFX(world, px, py, pz, px - pos.getX() - 0.5D, py - pos.getY() - 0.5D, pz - pos.getZ() - 0.5D, BuildCraftTransport.genericPipeBlock, random, meta);
					fx.setParticleIcon(icon);
					effectRenderer.addEffect(fx.applyColourMultiplier(pos.getX(), pos.getY(), pos.getZ()));*/
				}
			}
		}
		return true;
	}

	@Override
	public int colorMultiplier(IBlockAccess worldIn, BlockPos pos, int renderPass) {
		if (facadeRenderColor != -1) {
			return facadeRenderColor;
		}

		return super.colorMultiplier(worldIn, pos, renderPass);
	}

	/*TODO : Find a replacement @Override
	public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour) {
		TileGenericPipe pipeTile = (TileGenericPipe) world.getTileEntity(pos);
		if (!pipeTile.hasPlug(side)) {
			pipeTile.setColor(colour);
			return true;
		}

		return false;
	}*/

	public static void updateNeighbourSignalState(Pipe<?> pipe) {
		TileBuffer[] neighbours = pipe.container.getTileCache();

		if (neighbours != null) {
			for (int i = 0; i < 6; i++) {
				if (neighbours[i] != null && neighbours[i].getTile() instanceof TileGenericPipe &&
						!neighbours[i].getTile().isInvalid() &&
						((TileGenericPipe) neighbours[i].getTile()).pipe != null) {
					((TileGenericPipe) neighbours[i].getTile()).pipe.updateSignalState();
				}
			}
		}
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileGenericPipe) {
			return ((TileGenericPipe) tile).getState();
		} else {
			return state;
		}
	}

	/*@Override
	public IIcon getIcon(int side, int meta) {
		return PipeIconProvider.TYPE.PipeItemsStone.getIcon();
	}*/

	@Override
	public void registerModels() {

	}
}
