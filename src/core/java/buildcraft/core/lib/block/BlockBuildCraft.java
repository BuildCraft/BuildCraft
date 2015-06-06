/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.block;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

import buildcraft.api.core.BuildCraftProperties;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.api.events.BlockPlacedDownEvent;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.lib.utils.XorShift128Random;

public abstract class BlockBuildCraft extends BlockContainer {
	protected static boolean keepInventory = false;

	protected final XorShift128Random rand = new XorShift128Random();
	protected int renderPass;

	protected int maxPasses = 1;

	private boolean rotatable = false;
	private boolean alphaPass = false;

	public static final PropertyDirection FACING_PROP = BuildCraftProperties.BLOCK_FACING;
	public static final PropertyDirection FACING_6_PROP = BuildCraftProperties.BLOCK_FACING_6;

	public static final PropertyEnum COLOR_PROP = BuildCraftProperties.BLOCK_COLOR;
//	public static final PropertyEnum MACHINE_STATE = BuildCraftProperties.MACHINE_STATE;
//	public static final PropertyUnlistedEnum<EnumFillerPattern> FILLER_PATTERN = BuildCraftProperties.FILLER_PATTERN;

	public static final PropertyBool JOINED_BELOW = BuildCraftProperties.JOINED_BELOW;

	protected final IProperty[] properties;
	protected final HashBiMap<Integer, IBlockState> validStates = HashBiMap.create();

	private final BlockState myBlockState;

	protected BlockBuildCraft(Material material) {
		this(material, BCCreativeTab.get("main"), new IProperty[0], new IProperty[0]);
	}

	protected BlockBuildCraft(Material material, BCCreativeTab creativeTab) {
		this(material, creativeTab, new IProperty[0], new IProperty[0]);
	}

	protected BlockBuildCraft(Material material, IProperty[] properties) {
		this(material,BCCreativeTab.get("main"), properties, new IProperty[0]);
	}
	
	protected BlockBuildCraft(Material material, IProperty[] properties, IProperty[] nonMetaProperties) {
		this(material, BCCreativeTab.get("main"), properties, nonMetaProperties);
	}

	protected BlockBuildCraft(Material material, BCCreativeTab bcCreativeTab, IProperty[] properties, IProperty[] nonMetaProperties) {
		super(material);
		setCreativeTab(bcCreativeTab);
		setHardness(5F);

		this.properties = properties;

		this.myBlockState = createBlockState();

		IBlockState defaultState = getBlockState().getBaseState();

		int total = 1;
		List<IBlockState> tempValidStates = Lists.newArrayList();
		tempValidStates.add(defaultState);
		for (IProperty prop : properties) {
			total *= prop.getAllowedValues().size();
			if (total > 16)
				throw new IllegalArgumentException("Cannot have more than 16 properties in a block!");
			
			if (prop == FACING_6_PROP || prop == FACING_PROP) {
				rotatable = true;
			}
			
			Collection<Comparable<?>> allowedValues = prop.getAllowedValues();
			defaultState = defaultState.withProperty(prop, allowedValues.iterator().next());

			List<IBlockState> newValidStates = Lists.newArrayList();
			for (IBlockState state : tempValidStates) {
				for (Comparable<?> comp : allowedValues) {
					newValidStates.add(state.withProperty(prop, comp));
				}
			}
			tempValidStates = newValidStates;
		}

		int i = 0;
		for (IBlockState state : tempValidStates) {
			validStates.put(i, state);
			i++;
		}
		
		setDefaultState(defaultState);
	}

	@Override
	public BlockState getBlockState() {
		return this.myBlockState;
	}

	@Override
	protected BlockState createBlockState() {
		if (properties == null) {
			// Will be overridden later
			return new BlockState(this, new IProperty[] {});
		}

		return new BlockState(this, properties);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return validStates.inverse().get(state);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return validStates.get(meta);
	}

	public boolean hasAlphaPass() { return alphaPass; }

	public boolean isRotatable() {
		return rotatable;
	}

	public void setAlphaPass(boolean alphaPass) { this.alphaPass = alphaPass; }

	public void setPassCount(int maxPasses) {
		this.maxPasses = maxPasses;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, entity, stack);
		FMLCommonHandler.instance().bus().post(new BlockPlacedDownEvent((EntityPlayer) entity, pos, state));
		TileEntity tile = world.getTileEntity(pos);

		if (isRotatable()) {
			EnumFacing orientation = Utils.get2dOrientation(entity);
			world.setBlockState(pos, state.withProperty(FACING_PROP, orientation.getOpposite()));
		}

		if (tile instanceof TileBuildCraft) {
			((TileBuildCraft) tile).onBlockPlacedBy(entity, stack);
		}
	}

	@Override
	  public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
		BlockInteractionEvent event = new BlockInteractionEvent(player, state);
		FMLCommonHandler.instance().bus().post(event);
		if (event.isCanceled()) {
			return true;
		}

		return false;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		Utils.preDestroyBlock(world, pos);
		super.breakBlock(world, pos, state);
	}

	@Override
	public int getLightValue(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof IHasWork && ((IHasWork) tile).hasWork()) {
			return super.getLightValue(world, pos) + 8;
		} else {
			return super.getLightValue(world, pos);
		}
	}


	public boolean canRenderInPassBC(int pass) {
		if (pass >= maxPasses) {
			renderPass = 0;
			return false;
		} else {
			renderPass = pass;
			return true;
		}
	}

	@Override
	public int getRenderType() {
		return 3;
	}

	public int getCurrentRenderPass() {
		return renderPass;
	}

	public int getIconGlowLevel() {
		return -1;
	}

	public int getIconGlowLevel(IBlockAccess access, int x, int y, int z) {
		return getIconGlowLevel();
	}

	public int getFrontSide(int meta) {
		if (!isRotatable()) {
			return -1;
		}
		return meta >= 2 && meta <= 5 ? meta : 3;
	}
}
