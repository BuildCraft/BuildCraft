/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.logging.log4j.Level;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.BCLog;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.IItemPipe;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.utils.ColorUtils;
import buildcraft.core.utils.IModelRegister;
import buildcraft.core.utils.StringUtils;

public class ItemPipe extends ItemBuildCraft implements IItemPipe, IModelRegister {

	/*@SideOnly(Side.CLIENT)
	private IIconProvider iconProvider;*/
	private int pipeIconIndex;

	protected ItemPipe(CreativeTabBuildCraft creativeTab) {
		super(creativeTab);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		int index = side.getIndex();
		Block block = BuildCraftTransport.genericPipeBlock;

		int i = pos.getX();
		int j = pos.getY();
		int k = pos.getZ();

		IBlockState state = world.getBlockState(pos);
		Block worldBlock = state.getBlock();

		if (worldBlock == Blocks.snow) {
			index = 1;
		} else if (worldBlock != Blocks.vine && worldBlock != Blocks.tallgrass && worldBlock != Blocks.deadbush
				&& (worldBlock == null || !worldBlock.isReplaceable(world, pos))) {
			if (index == 0) {
				j--;
			}
			if (index == 1) {
				j++;
			}
			if (index == 2) {
				k--;
			}
			if (index == 3) {
				k++;
			}
			if (index == 4) {
				i--;
			}
			if (index == 5) {
				i++;
			}
		}

		if (itemstack.stackSize == 0) {
			return false;
		}

		pos = new BlockPos(i, j, k);

		if (world.canBlockBePlaced(block, pos, false, side, player, itemstack)) {
			Pipe<?> pipe = BlockGenericPipe.createPipe(this);

			if (pipe == null) {
				BCLog.logger.log(Level.WARN, "Pipe failed to create during placement at {0},{1},{2}", i, j, k);
				return true;
			}
			
			if (BlockGenericPipe.placePipe(pipe, world, new BlockPos(i, j, k), block, 0, player)) {
				block.onBlockPlacedBy(world, pos, block.getDefaultState(), player, itemstack);

				if (!world.isRemote) {
					TileEntity tile = world.getTileEntity(pos);
					((TileGenericPipe) tile).initializeFromItemMetadata(itemstack.getItemDamage());
				}

				world.playSoundEffect(i + 0.5F, j + 0.5F, k + 0.5F,
						block.stepSound.getPlaceSound(),
						(block.stepSound.getVolume() + 1.0F) / 2.0F,
						block.stepSound.getFrequency() * 0.8F);

				itemstack.stackSize--;
			}

			return true;
		} else {
			return false;
		}
	}

	/*@SideOnly(Side.CLIENT)
	public void setPipesIcons(IIconProvider iconProvider) {
		this.iconProvider = iconProvider;
	}*/

	public void setPipeIconIndex(int index) {
		this.pipeIconIndex = index;
	}

	public int getPipeIconIndex() {
		return this.pipeIconIndex;
	}

	/*@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1) {
		if (iconProvider != null) { // invalid pipes won't have this set
			return iconProvider.getIcon(pipeIconIndex);
		} else {
			return null;
		}
	}

	/*@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		// NOOP
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber() {
		return 0;
	}*/

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean advanced) {
		super.addInformation(stack, player, list, advanced);
		if (stack.getItemDamage() >= 1) {
			int color = (stack.getItemDamage() - 1) & 15;
			list.add(ColorUtils.getFormattingTooltip(color) + EnumChatFormatting.ITALIC + StringUtils.localize("color." + ColorUtils.getName(color)));
		}
		Class<? extends Pipe> pipe = BlockGenericPipe.pipes.get(this);
		List<String> toolTip = PipeToolTipManager.getToolTip(pipe, advanced);
		list.addAll(toolTip);
	}

	@Override
	public void registerModels() {

	}
}
