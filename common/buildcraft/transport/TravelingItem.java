/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import java.util.EnumSet;
import java.util.Map;

import com.google.common.collect.MapMaker;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.EnumColor;
import buildcraft.api.core.Position;
import buildcraft.core.inventory.StackHelper;

public class TravelingItem {

	public static final TravelingItemCache serverCache = new TravelingItemCache();
	public static final TravelingItemCache clientCache = new TravelingItemCache();
	public static final InsertionHandler DEFAULT_INSERTION_HANDLER = new InsertionHandler();
	private static int maxId = 0;

	public final EnumSet<EnumFacing> blacklist = EnumSet.noneOf(EnumFacing.class);

	public double xCoord, yCoord, zCoord;
	public final int id;
	public boolean toCenter = true;
	public EnumColor color;
	public EnumFacing input;
	public EnumFacing output;

	protected float speed = 0.01F;

	protected ItemStack itemStack;
	protected TileEntity container;
	protected NBTTagCompound extraData;
	protected InsertionHandler insertionHandler = DEFAULT_INSERTION_HANDLER;

	/* CONSTRUCTORS */
	protected TravelingItem(int id) {
		this.id = id;
	}

	public static TravelingItem make(int id) {
		TravelingItem item = new TravelingItem(id);
		getCache().cache(item);
		return item;
	}

	public static TravelingItem make() {
		return make(maxId < Short.MAX_VALUE ? ++maxId : (maxId = Short.MIN_VALUE));
	}

	public static TravelingItem make(double x, double y, double z, ItemStack stack) {
		TravelingItem item = make();
		item.xCoord = x;
		item.yCoord = y;
		item.zCoord = z;
		item.itemStack = stack.copy();
		return item;
	}

	public static TravelingItem make(NBTTagCompound nbt) {
		TravelingItem item = make();
		item.readFromNBT(nbt);
		return item;
	}

	public static TravelingItemCache getCache() {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			return clientCache;
		}
		return serverCache;
	}

	/* GETTING & SETTING */
	public void setPosition(double x, double y, double z) {
		this.xCoord = x;
		this.yCoord = y;
		this.zCoord = z;
	}

	public void movePosition(double x, double y, double z) {
		this.xCoord += x;
		this.yCoord += y;
		this.zCoord += z;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public ItemStack getItemStack() {
		return itemStack;
	}

	public void setItemStack(ItemStack item) {
		this.itemStack = item;
	}

	public TileEntity getContainer() {
		return container;
	}

	public void setContainer(TileEntity container) {
		this.container = container;
	}

	public NBTTagCompound getExtraData() {
		if (extraData == null) {
			extraData = new NBTTagCompound();
		}
		return extraData;
	}

	public boolean hasExtraData() {
		return extraData != null;
	}

	@Deprecated
	public void setInsetionHandler(InsertionHandler handler) {
		if (handler == null) {
			return;
		}
		this.insertionHandler = handler;
	}

	public void setInsertionHandler(InsertionHandler handler) {
		if (handler == null) {
			return;
		}
		this.insertionHandler = handler;
	}

	public InsertionHandler getInsertionHandler() {
		return insertionHandler;
	}

	public void reset() {
		toCenter = true;
		blacklist.clear();
		input = null;
		output = null;
	}

	/* SAVING & LOADING */
	public void readFromNBT(NBTTagCompound data) {
		setPosition(data.getDouble("x"), data.getDouble("y"), data.getDouble("z"));

		setSpeed(data.getFloat("speed"));
		setItemStack(ItemStack.loadItemStackFromNBT(data.getCompoundTag("Item")));

		toCenter = data.getBoolean("toCenter");
		input = EnumFacing.getFront(data.getByte("input"));
		output = EnumFacing.getFront(data.getByte("output"));

		byte c = data.getByte("color");
		if (c != -1) {
			color = EnumColor.fromId(c);
		}

		if (data.hasKey("extraData")) {
			extraData = data.getCompoundTag("extraData");
		}
	}

	public void writeToNBT(NBTTagCompound data) {
		data.setDouble("x", xCoord);
		data.setDouble("y", yCoord);
		data.setDouble("z", zCoord);
		data.setFloat("speed", getSpeed());
		NBTTagCompound itemStackTag = new NBTTagCompound();
		getItemStack().writeToNBT(itemStackTag);
		data.setTag("Item", itemStackTag);

		data.setBoolean("toCenter", toCenter);
		data.setByte("input", (byte) input.ordinal());
		data.setByte("output", (byte) output.ordinal());

		data.setByte("color", color != null ? (byte) color.ordinal() : -1);

		if (extraData != null) {
			data.setTag("extraData", extraData);
		}
	}

	public EntityItem toEntityItem() {
		if (container != null && !container.getWorld().isRemote) {
			if (getItemStack().stackSize <= 0) {
				return null;
			}

			Position motion = new Position(0, 0, 0, output);
			motion.moveForwards(0.1 + getSpeed() * 2F);

			EntityItem entity = new EntityItem(container.getWorld(), xCoord, yCoord, zCoord, getItemStack());

			entity.lifespan = BuildCraftCore.itemLifespan;
			entity.setDefaultPickupDelay();

			float f3 = 0.00F + container.getWorld().rand.nextFloat() * 0.04F - 0.02F;
			entity.motionX = (float) container.getWorld().rand.nextGaussian() * f3 + motion.x;
			entity.motionY = (float) container.getWorld().rand.nextGaussian() * f3 + motion.y;
			entity.motionZ = (float) container.getWorld().rand.nextGaussian() * f3 + +motion.z;
			return entity;
		}
		return null;
	}

	public boolean isCorrupted() {
		return itemStack == null || itemStack.stackSize <= 0 || itemStack.getItem() == null;
	}

	public boolean canBeGroupedWith(TravelingItem otherItem) {
		if (otherItem == this) {
			return false;
		}
		if (toCenter != otherItem.toCenter) {
			return false;
		}
		if (output != otherItem.output) {
			return false;
		}
		if (color != otherItem.color) {
			return false;
		}
		if (hasExtraData() || otherItem.hasExtraData()) {
			return false;
		}
		if (insertionHandler != DEFAULT_INSERTION_HANDLER) {
			return false;
		}
		if (!blacklist.equals(otherItem.blacklist)) {
			return false;
		}
		if (otherItem.isCorrupted()) {
			return false;
		}
		return StackHelper.canStacksMerge(itemStack, otherItem.itemStack);
	}

	public boolean tryMergeInto(TravelingItem otherItem) {
		if (!canBeGroupedWith(otherItem)) {
			return false;
		}
		if (StackHelper.mergeStacks(itemStack, otherItem.itemStack, false) == itemStack.stackSize) {
			StackHelper.mergeStacks(itemStack, otherItem.itemStack, true);
			itemStack.stackSize = 0;
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + this.id;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final TravelingItem other = (TravelingItem) obj;
		if (this.id != other.id) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "TravelingItem: " + id;
	}

	public static class InsertionHandler {

		public boolean canInsertItem(TravelingItem item, IInventory inv) {
			return true;
		}
	}

	public static class TravelingItemCache {

		private final Map<Integer, TravelingItem> itemCache = new MapMaker().weakValues().makeMap();

		public void cache(TravelingItem item) {
			itemCache.put(item.id, item);
		}

		public TravelingItem get(int id) {
			return itemCache.get(id);
		}
	}
}
