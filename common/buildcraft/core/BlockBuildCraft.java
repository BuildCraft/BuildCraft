/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import buildcraft.core.utils.Utils;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import buildcraft.api.tools.IToolWrench;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.item.Item;
import org.lwjgl.input.Keyboard;

public abstract class BlockBuildCraft extends Block implements ITileEntityProvider{

	protected static boolean keepInventory = false;
	protected final Random rand = new Random();

	protected BlockBuildCraft(Material material, CreativeTabBuildCraft creativeTab) {
		super(material);
		setCreativeTab(creativeTab.get());
		setHardness(5F);
		setHarvestLevel("wrench", 5);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack) {
		super.onBlockPlacedBy(world, x, y, z, entity, stack);
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileBuildCraft) {
			((TileBuildCraft) tile).onBlockPlacedBy(entity, stack);
		}
	}

	protected Item getItemToStoreData(World wrd, int x, int y, int z){
		return Item.getItemFromBlock(this);
	}

	@Override
	public void harvestBlock(World wrd, EntityPlayer player, int x, int y, int z, int meta) {}

	public void addDescription(NBTTagCompound nbt, List<String> lines, boolean f3) {
		lines.add(I18n.format("tip.nbt")); // Default
	}

	@Override
	public void onBlockHarvested(World wrd, int x, int y, int z, int meta, EntityPlayer player) {
		if (player.capabilities.isCreativeMode) {
			Utils.preDestroyBlock(wrd, x, y, z);
			return;
		}
		if (player.getHeldItem() != null && player.isSneaking() && player.getHeldItem().getItem() instanceof IToolWrench) {
			player.addStat(StatList.mineBlockStatArray[getIdFromBlock(this)], 1);
			player.addExhaustion(0.025F);

			ArrayList<ItemStack> drops = getDrops(wrd, x, y, z, meta, EnchantmentHelper.getFortuneModifier(player));
			ForgeEventFactory.fireBlockHarvesting(drops, wrd, this, x, y, z, meta, 0, 1.0F, true, player);
			Item storage = getItemToStoreData(wrd, x, y, z);
			boolean flag = false;
			for (ItemStack is : drops) {
				if (!flag && is != null && is.getItem() == storage) {
					storeData(is, wrd, x, y, z);
					flag = true;
				}
				dropBlockAsItem(wrd, x, y, z, is);
			}
			if (!flag) {
				Utils.preDestroyBlock(wrd, x, y, z);
			}
		} else {
			Utils.preDestroyBlock(wrd, x, y, z);
			super.harvestBlock(wrd, player, x, y, z, meta);
		}
	}

	protected void storeData(ItemStack is, World wrd, int x, int y, int z){
		TileEntity tile = wrd.getTileEntity(x, y, z);
		if (tile != null) {
			if (!is.hasTagCompound()) {
				is.setTagCompound(new NBTTagCompound());
			}
			NBTTagCompound nbt = new NBTTagCompound();
			tile.writeToNBT(nbt);
			if (tile instanceof ISaveExclusions) {
				for (String exclusion : ((ISaveExclusions) tile).getExclusions()) {
					nbt.removeTag(exclusion);
				}
			}
			nbt.removeTag("x");
			nbt.removeTag("y");
			nbt.removeTag("z");
			nbt.removeTag("owner");
			is.getTagCompound().setTag("tileData", nbt);
		}
	}

	protected boolean forceMiddleClickSaving() { //Exclusively for tanks
		return false;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition mop, World wrd, int x, int y, int z) {
		if (Keyboard.isKeyDown(Minecraft.getMinecraft().gameSettings.keyBindSprint.getKeyCode()) && //TODO Not sure about the server!
				(forceMiddleClickSaving() || getItemToStoreData(wrd, x, y, z) == Item.getItemFromBlock(this))) {
			ItemStack is = super.getPickBlock(mop, wrd, x, y, z);
			TileEntity tile = wrd.getTileEntity(x, y, z);
			if (tile != null){
				NBTTagCompound nbt = new NBTTagCompound();
				tile.writeToNBT(nbt);
				if (!is.hasTagCompound()){
					is.setTagCompound(new NBTTagCompound());
				}
				is.getTagCompound().setTag("tileData", nbt);
			}
			return is;
		} else {
			return super.getPickBlock(mop, wrd, x, y, z);
		}
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof IMachine && ((IMachine) tile).isActive())
			return super.getLightValue(world, x, y, z) + 8;
		return super.getLightValue(world, x, y, z);
	}
}
