/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import buildcraft.api.core.BuildCraftProperties;
import buildcraft.api.events.BlockPlacedDownEvent;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.utils.Utils;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;

public abstract class BlockBuildCraft extends BlockContainer {

	protected static boolean keepInventory = false;
	protected final Random rand = new Random();
	
	public static final PropertyDirection FACING_PROP = BuildCraftProperties.BLOCK_FACING;
	public static final PropertyDirection FACING_6_PROP = BuildCraftProperties.BLOCK_FACING_6;

	public static final PropertyEnum COLOR_PROP = BuildCraftProperties.BLOCK_COLOR;
	public static final PropertyEnum MACHINE_STATE = BuildCraftProperties.MACHINE_STATE;
	
	public static final PropertyBool JOINED_BELOW = BuildCraftProperties.JOINED_BELOW;

	protected final IProperty[] properties;
	protected final HashBiMap<Integer, IBlockState> validStates = HashBiMap.create();

	private final BlockState myBlockState;

	protected BlockBuildCraft(Material material) {
		this(material, CreativeTabBuildCraft.BLOCKS, new IProperty[]{});
	}

	protected BlockBuildCraft(Material material, CreativeTabBuildCraft creativeTab) {
		this(material, creativeTab, new IProperty[0]);
	}

	protected BlockBuildCraft(Material material, IProperty[] properties) {
		this(material, CreativeTabBuildCraft.BLOCKS, properties);
	}

	protected BlockBuildCraft(Material material, CreativeTabBuildCraft creativeTab, IProperty[] properties) {
		super(material);
		setCreativeTab(creativeTab.get());
		setHardness(5F);

		this.properties = properties;

		this.myBlockState = createBlockState();

		IBlockState defaultState = getBlockState().getBaseState();

		int total = 1;
		List<IBlockState> tempValidStates = Lists.newArrayList();
		tempValidStates.add(defaultState);
		for (IProperty prop: properties) {
		    total *= prop.getAllowedValues().size();
		    if (total > 16)
		        throw new IllegalArgumentException("Cannot have more than 16 properties in a block!");
		    Collection<Comparable<?>> allowedValues = prop.getAllowedValues();
		    defaultState = defaultState.withProperty(prop, allowedValues.iterator().next());

		    List<IBlockState> newValidStates = Lists.newArrayList();
		    for (IBlockState state: tempValidStates) {
		        for (Comparable<?> comp : allowedValues) {
		            newValidStates.add(state.withProperty(prop, comp));
		        }
		    }
		    tempValidStates = newValidStates;
		}

		int i = 0;
		for (IBlockState state: tempValidStates) {
		    validStates.put(i, state);
	        i++;
		}

		setDefaultState(defaultState);
	}

	@Override
	public BlockState getBlockState()
	{
		return this.myBlockState;
	}

	@Override
	protected BlockState createBlockState() {
		if (properties == null) {
			// Will be overridden later
			return new BlockState(this, new IProperty[]{});
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

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, entity, stack);
		FMLCommonHandler.instance().bus().post(new BlockPlacedDownEvent((EntityPlayer) entity, world.getBlockState(pos), pos));
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileBuildCraft) {
			((TileBuildCraft) tile).onBlockPlacedBy(entity, stack);
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		Utils.preDestroyBlock(world, pos, state);
		super.breakBlock(world, pos, state);
	}

	@Override
	public int getLightValue(IBlockAccess world, BlockPos pos) {
		if (hasTileEntity(world.getBlockState(pos))) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof IHasWork && ((IHasWork) tile).hasWork()) {
				return super.getLightValue(world, pos) + 8;
			}
		}
		return super.getLightValue(world, pos);
	}
	
    public void dropItemStack(World world, BlockPos pos, ItemStack itemstack)
    {
        float f = RANDOM.nextFloat() * 0.8F + 0.1F;
        float f1 = RANDOM.nextFloat() * 0.8F + 0.1F;
        float f2 = RANDOM.nextFloat() * 0.8F + 0.1F;

        while (itemstack.stackSize > 0)
        {
            int i = RANDOM.nextInt(21) + 10;

            if (i > itemstack.stackSize)
            {
                i = itemstack.stackSize;
            }

            itemstack.stackSize -= i;
            EntityItem entityitem = new EntityItem(world, pos.getX() + (double)f, pos.getY() + (double)f1, pos.getZ() + (double)f2, new ItemStack(itemstack.getItem(), i, itemstack.getMetadata()));

            if (itemstack.hasTagCompound())
            {
                entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
            }

            float f3 = 0.05F;
            entityitem.motionX = RANDOM.nextGaussian() * (double)f3;
            entityitem.motionY = RANDOM.nextGaussian() * (double)f3 + 0.20000000298023224D;
            entityitem.motionZ = RANDOM.nextGaussian() * (double)f3;
            world.spawnEntityInWorld(entityitem);
        }
    }

	@Override
	public int getRenderType() {
		return 3;
	}
}
