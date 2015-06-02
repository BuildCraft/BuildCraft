/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import buildcraft.core.CoreConstants;
import buildcraft.core.IFramePipeConnection;
import buildcraft.core.utils.PropertyBoolUnlisted;
import buildcraft.core.utils.Utils;

import com.google.common.collect.Maps;

public class BlockFrame extends Block implements IFramePipeConnection {

	public static final PropertyBoolUnlisted UP_PROP = new PropertyBoolUnlisted("up");
	public static final PropertyBoolUnlisted DOWN_PROP = new PropertyBoolUnlisted("down");
	public static final PropertyBoolUnlisted WEST_PROP = new PropertyBoolUnlisted("west");
	public static final PropertyBoolUnlisted EAST_PROP = new PropertyBoolUnlisted("east");
	public static final PropertyBoolUnlisted NORTH_PROP =new PropertyBoolUnlisted("north");
	public static final PropertyBoolUnlisted SOUTH_PROP = new PropertyBoolUnlisted("south");
	
	public static final Map<EnumFacing, PropertyBoolUnlisted> FACING_PROPS;
	
	static {
		Map<EnumFacing, PropertyBoolUnlisted> map = Maps.newEnumMap(EnumFacing.class);

		map.put(EnumFacing.DOWN, DOWN_PROP);
		map.put(EnumFacing.UP, UP_PROP);

		map.put(EnumFacing.EAST, EAST_PROP);
		map.put(EnumFacing.WEST, WEST_PROP);

		map.put(EnumFacing.NORTH, NORTH_PROP);
		map.put(EnumFacing.SOUTH, SOUTH_PROP);

		FACING_PROPS = Collections.unmodifiableMap(map);
	}
	
	
	public BlockFrame() {
		super(Material.glass);
		setHardness(0.5F);
	}
	
	@Override
	protected BlockState createBlockState() {
		IUnlistedProperty<?>[] props = new IUnlistedProperty[] { 
			UP_PROP, DOWN_PROP, WEST_PROP, EAST_PROP, NORTH_PROP, SOUTH_PROP};
        return new ExtendedBlockState(this, new IProperty[0], props);
    }

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (world.isRemote) {
			return;
		}

		removeNeighboringFrames(world, pos);
	}

	protected void removeNeighboringFrames(World world, BlockPos pos) {
		for (EnumFacing dir : EnumFacing.values()) {
			BlockPos nPos = pos.offset(dir);
			Block nBlock = world.getBlockState(nPos).getBlock();
			if (nBlock == this) {
				world.setBlockToAir(nPos);
			}
		}
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean isFullCube() { return false; }

	@Override
	public Item getItemDropped(IBlockState i, Random random, int j) {
		return null;
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return new ArrayList<ItemStack>();
	}
	
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		for (Entry<EnumFacing, PropertyBoolUnlisted> entry : FACING_PROPS.entrySet()) {
			boolean connects = Utils.checkLegacyPipesConnections(world, pos, pos.offset(entry.getKey()));
			state = state.withProperty(entry.getValue(), connects);
		}
        return state;
    }

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
		float xMin = CoreConstants.PIPE_MIN_POS, xMax = CoreConstants.PIPE_MAX_POS, yMin = CoreConstants.PIPE_MIN_POS, yMax = CoreConstants.PIPE_MAX_POS, zMin = CoreConstants.PIPE_MIN_POS, zMax = CoreConstants.PIPE_MAX_POS;

		if (Utils.checkLegacyPipesConnections(world, pos, pos.west())) {
			xMin = 0.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, pos, pos.east())) {
			xMax = 1.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, pos, pos.down())) {
			yMin = 0.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, pos, pos.up())) {
			yMax = 1.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, pos, pos.north())) {
			zMin = 0.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, pos, pos.south())) {
			zMax = 1.0F;
		}

		return AxisAlignedBB.fromBounds((double) pos.getX() + xMin, (double) pos.getY() + yMin, (double) pos.getZ() + zMin, (double) pos.getX() + xMax, (double) pos.getY() + yMax, (double) pos.getZ() + zMax);
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos) {
		return getCollisionBoundingBox(world, pos, null);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity) {
		setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
		super.addCollisionBoxesToList(world, pos, state, axisalignedbb, arraylist, par7Entity);

		if (Utils.checkLegacyPipesConnections(world, pos, pos.west())) {
			setBlockBounds(0.0F, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
			super.addCollisionBoxesToList(world, pos, state, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkLegacyPipesConnections(world, pos, pos.east())) {
			setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, 1.0F, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
			super.addCollisionBoxesToList(world, pos, state, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkLegacyPipesConnections(world, pos, pos.down())) {
			setBlockBounds(CoreConstants.PIPE_MIN_POS, 0.0F, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
			super.addCollisionBoxesToList(world, pos, state, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkLegacyPipesConnections(world, pos, pos.up())) {
			setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, 1.0F, CoreConstants.PIPE_MAX_POS);
			super.addCollisionBoxesToList(world, pos, state, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkLegacyPipesConnections(world, pos, pos.north())) {
			setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, 0.0F, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS);
			super.addCollisionBoxesToList(world, pos, state, axisalignedbb, arraylist, par7Entity);
		}

		if (Utils.checkLegacyPipesConnections(world, pos, pos.south())) {
			setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, 1.0F);
			super.addCollisionBoxesToList(world, pos, state, axisalignedbb, arraylist, par7Entity);
		}

		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 vec3d, Vec3 vec3d1) {
		float xMin = CoreConstants.PIPE_MIN_POS, xMax = CoreConstants.PIPE_MAX_POS, yMin = CoreConstants.PIPE_MIN_POS, yMax = CoreConstants.PIPE_MAX_POS, zMin = CoreConstants.PIPE_MIN_POS, zMax = CoreConstants.PIPE_MAX_POS;

		if (Utils.checkLegacyPipesConnections(world, pos, pos.west())) {
			xMin = 0.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, pos, pos.east())) {
			xMax = 1.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, pos, pos.down())) {
			yMin = 0.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, pos, pos.up())) {
			yMax = 1.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, pos, pos.north())) {
			zMin = 0.0F;
		}

		if (Utils.checkLegacyPipesConnections(world, pos, pos.south())) {
			zMax = 1.0F;
		}

		setBlockBounds(xMin, yMin, zMin, xMax, yMax, zMax);

		MovingObjectPosition r = super.collisionRayTrace(world, pos, vec3d, vec3d1);

		setBlockBounds(0, 0, 0, 1, 1, 1);

		return r;
	}

	@Override
	public boolean isPipeConnected(IBlockAccess blockAccess, BlockPos pos, BlockPos pos1) {
		return blockAccess.getBlockState(pos1).getBlock() == this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(this));
	}
}
