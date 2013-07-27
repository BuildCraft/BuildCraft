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
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.ForgeDirection;

public class TravelingItem {

	private static int maxId = 0;
	protected float speed = 0.01F;
	protected ItemStack item;
	protected TileEntity container;
	protected Position position;
	public final int id;
	public boolean toCenter = true;
	public EnumColor color;
	public ForgeDirection input = ForgeDirection.UNKNOWN;
	public ForgeDirection output = ForgeDirection.UNKNOWN;
	public final EnumSet<ForgeDirection> blacklist = EnumSet.noneOf(ForgeDirection.class);
	private NBTTagCompound extraData;

	/* CONSTRUCTORS */
	public TravelingItem() {
		this(maxId < Short.MAX_VALUE ? ++maxId : (maxId = Short.MIN_VALUE));
	}

	public TravelingItem(int id) {
		this.id = id;
	}

	public TravelingItem(double x, double y, double z, ItemStack stack) {
		this();
		this.position = new Position(x, y, z);
		this.item = stack.copy();
	}

	/* GETTING & SETTING */
	public Position getPosition() {
		return position;
	}

	public void setPosition(double x, double y, double z) {
		position = new Position(x, y, z);
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

		NBTTagList contribList = data.getTagList("contribList");

		for (int i = 0; i < contribList.tagCount(); ++i) {
			NBTTagCompound cpt = (NBTTagCompound) contribList.tagAt(i);
			String key = cpt.getString("key");

			String className = cpt.getString("class");

			if (getClass().getName().startsWith("net.minecraft.src")) {
				className = "net.minecraft.src." + className;
			}

			if (data.hasKey("extraData"))
				extraData = data.getCompoundTag("extraData");
		}
	}

	public void writeToNBT(NBTTagCompound data) {
		data.setDouble("x", position.x);
		data.setDouble("y", position.y);
		data.setDouble("z", position.z);
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

	public EntityItem toEntityItem(ForgeDirection dir) {
		if (container != null && !CoreProxy.proxy.isRenderWorld(container.worldObj)) {
			if (getItemStack().stackSize <= 0)
				return null;

			Position motion = new Position(0, 0, 0, dir);
			motion.moveForwards(0.1 + getSpeed() * 2F);

			EntityItem entityitem = new EntityItem(container.worldObj, position.x, position.y, position.z, getItemStack());

			entityitem.lifespan = BuildCraftCore.itemLifespan;
			entityitem.delayBeforeCanPickup = 10;

			float f3 = 0.00F + container.worldObj.rand.nextFloat() * 0.04F - 0.02F;
			entityitem.motionX = (float) container.worldObj.rand.nextGaussian() * f3 + motion.x;
			entityitem.motionY = (float) container.worldObj.rand.nextGaussian() * f3 + motion.y;
			entityitem.motionZ = (float) container.worldObj.rand.nextGaussian() * f3 + +motion.z;
			container.worldObj.spawnEntityInWorld(entityitem);
			return entityitem;
		}
		return null;
	}

	public float getEntityBrightness(float f) {
		int i = MathHelper.floor_double(position.x);
		int j = MathHelper.floor_double(position.z);
		if (container.worldObj.blockExists(i, 128 / 2, j)) {
			double d = 0.66000000000000003D;
			int k = MathHelper.floor_double(position.y + d);
			return container.worldObj.getLightBrightness(i, k, j);
		} else
			return 0.0F;
	}

	public boolean isCorrupted() {
		return getItemStack() == null || getItemStack().stackSize <= 0 || Item.itemsList[getItemStack().itemID] == null;
	}

	/**
	 * @return the can this item be moved into this specific inventory type.
	 * (basic BuildCraft PipedItems always return true)
	 */
	public boolean canSinkTo(TileEntity entity) {
		return true;
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
}
