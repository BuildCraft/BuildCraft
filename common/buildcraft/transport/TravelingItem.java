/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.Position;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.EnumColor;
import java.util.EnumSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeDirection;

public class TravelingItem {

	public static final InsertionHandler DEFAULT_INSERTION_HANDLER = new InsertionHandler();
	private static int maxId = 0;
	protected float speed = 0.01F;
	protected ItemStack item;
	protected TileEntity container;
	public double xCoord, yCoord, zCoord;
	public final int id;
	public boolean toCenter = true;
	public EnumColor color;
	public ForgeDirection input = ForgeDirection.UNKNOWN;
	public ForgeDirection output = ForgeDirection.UNKNOWN;
	public final EnumSet<ForgeDirection> blacklist = EnumSet.noneOf(ForgeDirection.class);
	private NBTTagCompound extraData;
	private InsertionHandler insertionHandler = DEFAULT_INSERTION_HANDLER;

	/* CONSTRUCTORS */
	public TravelingItem() {
		this(maxId < Short.MAX_VALUE ? ++maxId : (maxId = Short.MIN_VALUE));
	}

	public TravelingItem(int id) {
		this.id = id;
	}

	public TravelingItem(double x, double y, double z, ItemStack stack) {
		this();
		this.xCoord = x;
		this.yCoord = y;
		this.zCoord = z;
		this.item = stack.copy();
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
		return item;
	}

	public void setItemStack(ItemStack item) {
		this.item = item;
	}

	public TileEntity getContainer() {
		return container;
	}

	public void setContainer(TileEntity container) {
		this.container = container;
	}

	public NBTTagCompound getExtraData() {
		if (extraData == null)
			extraData = new NBTTagCompound();
		return extraData;
	}

	public boolean hasExtraData() {
		return extraData != null;
	}

	@Deprecated
	public void setInsetionHandler(InsertionHandler handler) {
		if (handler == null)
			return;
		this.insertionHandler = handler;
	}

	public void setInsertionHandler(InsertionHandler handler) {
		if (handler == null)
			return;
		this.insertionHandler = handler;
	}

	public InsertionHandler getInsertionHandler() {
		return insertionHandler;
	}

	public void reset() {
		toCenter = true;
		blacklist.clear();
		input = ForgeDirection.UNKNOWN;
		output = ForgeDirection.UNKNOWN;
	}

	/* SAVING & LOADING */
	public void readFromNBT(NBTTagCompound data) {
		setPosition(data.getDouble("x"), data.getDouble("y"), data.getDouble("z"));

		setSpeed(data.getFloat("speed"));
		setItemStack(ItemStack.loadItemStackFromNBT(data.getCompoundTag("Item")));

		toCenter = data.getBoolean("toCenter");
		input = ForgeDirection.getOrientation(data.getInteger("input"));
		output = ForgeDirection.getOrientation(data.getInteger("output"));

		byte c = data.getByte("color");
		if (c != -1)
			color = EnumColor.fromId(c);

		if (data.hasKey("extraData"))
			extraData = data.getCompoundTag("extraData");
	}

	public void writeToNBT(NBTTagCompound data) {
		data.setDouble("x", xCoord);
		data.setDouble("y", yCoord);
		data.setDouble("z", zCoord);
		data.setFloat("speed", getSpeed());
		NBTTagCompound nbttagcompound2 = new NBTTagCompound();
		getItemStack().writeToNBT(nbttagcompound2);
		data.setCompoundTag("Item", nbttagcompound2);

		data.setBoolean("toCenter", toCenter);
		data.setInteger("input", input.ordinal());
		data.setInteger("output", output.ordinal());

		data.setByte("color", color != null ? (byte) color.ordinal() : -1);

		if (extraData != null)
			data.setTag("extraData", extraData);
	}

	public EntityItem toEntityItem() {
		if (container != null && !CoreProxy.proxy.isRenderWorld(container.worldObj)) {
			if (getItemStack().stackSize <= 0)
				return null;

			Position motion = new Position(0, 0, 0, output);
			motion.moveForwards(0.1 + getSpeed() * 2F);

			ItemStack stack = getItemStack();
			EntityItem entity = new EntityItem(container.worldObj, xCoord, yCoord, zCoord, getItemStack());
			if (stack.getItem().hasCustomEntity(stack)) {
				Entity e = stack.getItem().createEntity(container.worldObj, entity, stack);
				if (e instanceof EntityItem)
					entity = (EntityItem) e;
			}

			entity.lifespan = BuildCraftCore.itemLifespan;
			entity.delayBeforeCanPickup = 10;

			float f3 = 0.00F + container.worldObj.rand.nextFloat() * 0.04F - 0.02F;
			entity.motionX = (float) container.worldObj.rand.nextGaussian() * f3 + motion.x;
			entity.motionY = (float) container.worldObj.rand.nextGaussian() * f3 + motion.y;
			entity.motionZ = (float) container.worldObj.rand.nextGaussian() * f3 + +motion.z;
			return entity;
		}
		return null;
	}

	public float getEntityBrightness(float f) {
		int i = MathHelper.floor_double(xCoord);
		int j = MathHelper.floor_double(zCoord);
		if (container != null && container.worldObj.blockExists(i, 128 / 2, j)) {
			double d = 0.66000000000000003D;
			int k = MathHelper.floor_double(yCoord + d);
			return container.worldObj.getLightBrightness(i, k, j);
		} else
			return 0.0F;
	}

	public boolean isCorrupted() {
		return getItemStack() == null || getItemStack().stackSize <= 0 || Item.itemsList[getItemStack().itemID] == null;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 67 * hash + this.id;
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TravelingItem other = (TravelingItem) obj;
		if (this.id != other.id)
			return false;
		return true;
	}

	public static class InsertionHandler {

		public boolean canInsertItem(TravelingItem item, IInventory inv) {
			return true;
		}
	}
}
