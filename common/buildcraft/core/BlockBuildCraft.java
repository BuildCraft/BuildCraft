/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import buildcraft.api.core.BuildCraftProperties;
import buildcraft.api.events.BlockPlacedDownEvent;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.utils.Utils;

public abstract class BlockBuildCraft extends BlockContainer {

	protected static boolean keepInventory = false;
	protected final Random rand = new Random();
	
	public static final PropertyDirection FACING_PROP = BuildCraftProperties.BLOCK_FACING;
	public static final PropertyDirection FACING_6_PROP = BuildCraftProperties.BLOCK_FACING_6;

	public static final PropertyEnum COLOR_PROP = BuildCraftProperties.BLOCK_COLOR;

	protected final IProperty[] properties;

	private final int[] propertySizes;
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
		this.propertySizes = new int[properties.length];

		this.myBlockState = createBlockState();

		IBlockState defaultState = getBlockState().getBaseState();
		for (int i = 0; i < properties.length; i++) {
			try {
				if (properties[i] instanceof PropertyBool) {
					propertySizes[i] = 2;
				} else if (properties[i] instanceof PropertyInteger) {
					// HACK! Only allows 4-bit integers
					propertySizes[i] = 16;
				} else if (properties[i].getValueClass().isEnum()) {
					propertySizes[i] = ((Enum[]) properties[i].getValueClass().getMethod("values").invoke(null)).length;
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			Object o = properties[i].getAllowedValues().iterator().next();
			defaultState = defaultState.withProperty(properties[i], (Comparable) o);
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
		int mul = 1;
		int val = 0;
		for (int i = 0; i < properties.length; i++) {
			if (properties[i] instanceof PropertyInteger) {
				val += ((Integer) state.getValue(properties[i])).intValue();
			} else if (properties[i] instanceof PropertyBool) {
				val += ((Boolean) state.getValue(properties[i])).booleanValue() ? 1 : 0;
			} else {
				val += ((Enum) state.getValue(properties[i])).ordinal() * mul;
			}
			mul *= propertySizes[i];
		}
		return val;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		IBlockState state = getDefaultState();
		int mul = 1;
		int prevMul = 1;
		int val = meta;
		for (int i = 0; i < properties.length; i++) {
			mul *= propertySizes[i];
			int enumVal = val % mul;
			val -= enumVal;
			val /= prevMul;
			try {
				if (properties[i] instanceof PropertyInteger) {
					state = state.withProperty(properties[i], val);
				} else if (properties[i] instanceof PropertyBool) {
					state = state.withProperty(properties[i], val > 0);
				} else if (properties[i].getValueClass() == EnumFacing.class) {
					state = state.withProperty(properties[i], EnumFacing.getFront(enumVal));
				} else {
					state = state.withProperty(properties[i], ((Enum[]) properties[i].getValueClass().getMethod("values").invoke(null))[enumVal]);
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
			prevMul = mul;
		}
		return state;
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
