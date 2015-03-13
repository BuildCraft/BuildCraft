/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.api.events.BlockInteractionEvent;
import buildcraft.api.events.BlockPlacedDownEvent;
import buildcraft.api.tiles.IHasWork;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.utils.ResourceUtils;
import buildcraft.core.lib.utils.Utils;

public abstract class BlockBuildCraft extends BlockContainer {
	@SideOnly(Side.CLIENT)
	public IIcon[][] icons = new IIcon[16][];

	protected static boolean keepInventory = false;
	protected final Random rand = new Random();
	protected int renderPass;

	protected int maxPasses = 1;

	private static final int[][] SIDE_TEXTURING_LOCATIONS = new int[][] {
			{2, 3, 5, 4},
			{3, 2, 4, 5},
			{4, 5, 2, 3},
			{5, 4, 3, 2}
	};
	private boolean rotatable = false;

	protected BlockBuildCraft(Material material) {
		this(material, BCCreativeTab.get("main"));
	}

	protected BlockBuildCraft(Material material, CreativeTabs creativeTab) {
		super(material);
		setCreativeTab(creativeTab);
		setHardness(5F);
	}

	public boolean isRotatable() {
		return rotatable;
	}

	public void setRotatable(boolean rotatable) {
		this.rotatable = rotatable;
	}

	public void setPassCount(int maxPasses) {
		this.maxPasses = maxPasses;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		super.onBlockPlacedBy(world, x, y, z, entity, stack);
		FMLCommonHandler.instance().bus().post(new BlockPlacedDownEvent((EntityPlayer) entity, world.getBlock(x, y, z), world.getBlockMetadata(x, y, z), x, y, z));
		TileEntity tile = world.getTileEntity(x, y, z);

		if (isRotatable()) {
			ForgeDirection orientation = Utils.get2dOrientation(entity);
			world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z) & 8 | orientation.getOpposite().ordinal(), 1);
		}

		if (tile instanceof TileBuildCraft) {
			((TileBuildCraft) tile).onBlockPlacedBy(entity, stack);
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int par6, float par7,
									float par8, float par9) {
		BlockInteractionEvent event = new BlockInteractionEvent(entityplayer, this);
		FMLCommonHandler.instance().bus().post(event);
		if (event.isCanceled()) {
			return true;
		}

		if (isRotatable()) {
			if (!entityplayer.isSneaking()) {
				Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
				if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, x, y, z)) {
					int meta = world.getBlockMetadata(x, y, z);

					switch (ForgeDirection.values()[meta & 7]) {
						case WEST:
							world.setBlockMetadataWithNotify(x, y, z, meta & 8 | ForgeDirection.SOUTH.ordinal(), 0);
							break;
						case EAST:
							world.setBlockMetadataWithNotify(x, y, z, meta & 8 | ForgeDirection.NORTH.ordinal(), 0);
							break;
						case NORTH:
							world.setBlockMetadataWithNotify(x, y, z, meta & 8 | ForgeDirection.WEST.ordinal(), 0);
							break;
						case SOUTH:
						default:
							world.setBlockMetadataWithNotify(x, y, z, meta & 8 | ForgeDirection.EAST.ordinal(), 0);
							break;
					}

					world.markBlockForUpdate(x, y, z);
					((IToolWrench) equipped).wrenchUsed(entityplayer, x, y, z);
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
		Utils.preDestroyBlock(world, x, y, z);
		super.breakBlock(world, x, y, z, block, par6);
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof IHasWork && ((IHasWork) tile).hasWork()) {
			return super.getLightValue(world, x, y, z) + 8;
		} else {
			return super.getLightValue(world, x, y, z);
		}
	}


	@Override
	public boolean rotateBlock(World world, int x, int y, int z, ForgeDirection axis) {
		// TODO: Actually look at the axis parameter
		int meta = world.getBlockMetadata(x, y, z);

		switch (ForgeDirection.getOrientation(meta)) {
			case WEST:
				world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.SOUTH.ordinal(), 3);
				break;
			case EAST:
				world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.NORTH.ordinal(), 3);
				break;
			case NORTH:
				world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.WEST.ordinal(), 3);
				break;
			case SOUTH:
			default:
				world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.EAST.ordinal(), 3);
				break;
		}
		world.markBlockForUpdate(x, y, z);
		return true;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconAbsolute(IBlockAccess access, int x, int y, int z, int side, int metadata) {
		return getIconAbsolute(side, metadata);
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconAbsolute(int side, int metadata) {
		return icons[metadata] == null ? icons[0][side] : icons[metadata][side];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess access, int x, int y, int z, int side) {
		IIcon icon;
		int metadata = access.getBlockMetadata(x, y, z);
		if (isRotatable()) {
			if (side < 2) {
				icon = getIconAbsolute(access, x, y, z, side, metadata & 8);
			} else {
				int front = metadata >= 2 && metadata <= 5 ? metadata : 3;
				icon = getIconAbsolute(access, x, y, z, SIDE_TEXTURING_LOCATIONS[(front - 2) % 4][(side - 2) % 4], metadata & 8);
			}
		} else {
			icon = getIconAbsolute(access, x, y, z, side, metadata);
		}
		return icon != null ? icon : BuildCraftCore.transparentTexture;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		if (isRotatable()) {
			if (side < 2) {
				return getIconAbsolute(side, metadata & 8);
			}

			int front = getFrontSide(metadata);
			return getIconAbsolute(SIDE_TEXTURING_LOCATIONS[(front - 2) % 4][(side - 2) % 4], metadata & 8);
		} else {
			return getIconAbsolute(side, metadata);
		}
	}

	@SideOnly(Side.CLIENT)
	protected void registerIconsForMeta(int meta, String blockName, IIconRegister register) {
		icons[meta] = new IIcon[6];
		String name = ResourceUtils.getObjectPrefix(blockName);
		icons[meta][0] = ResourceUtils.getIconPriority(register, name, new String[] {
				"bottom", "topbottom", "default"
		});
		icons[meta][1] = ResourceUtils.getIconPriority(register, name, new String[] {
				"top", "topbottom", "default"
		});
		icons[meta][2] = ResourceUtils.getIconPriority(register, name, new String[] {
				"front", "frontback", "side", "default"
		});
		icons[meta][3] = ResourceUtils.getIconPriority(register, name, new String[] {
				"back", "frontback", "side", "default"
		});
		icons[meta][4] = ResourceUtils.getIconPriority(register, name, new String[] {
				"left", "leftright", "side", "default"
		});
		icons[meta][5] = ResourceUtils.getIconPriority(register, name, new String[] {
				"right", "leftright", "side", "default"
		});
	}

	@SideOnly(Side.CLIENT)
	public String[] getIconBlockNames() {
		return new String[] {Block.blockRegistry.getNameForObject(this)};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		String[] iconBlockNames = getIconBlockNames();
		for (int i = 0; i < iconBlockNames.length; i++) {
			registerIconsForMeta(i, iconBlockNames[i], register);
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

	//@SideOnly(Side.CLIENT)
	//public int getRenderBlockPass() {
	//	return maxPasses > 1 ? 1 : 0;
	//}

	@Override
	public int getRenderType() {
		return (maxPasses > 1 || isRotatable()) ? BuildCraftCore.complexBlockModel : 0;
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
