/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core;

import java.util.TreeMap;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.Position;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.transport.IPassiveItemContribution;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.proxy.CoreProxy;

public class EntityPassiveItem implements IPipedItem {

	private static TreeMap<String, IPassiveItemContribution> contributions = new TreeMap<String, IPassiveItemContribution>();
	protected static int maxId = 0;
	protected World worldObj;

	protected float speed = 0.01F;
	protected ItemStack item;

	protected TileEntity container;

	@Deprecated
	protected SafeTimeTracker synchroTracker = new SafeTimeTracker();

	@Deprecated
	protected int deterministicRandomization = 0;

	protected Position position;
	protected int entityId;

	/* CONSTRUCTORS */
	public EntityPassiveItem(World world) {
		this(world, maxId < Short.MAX_VALUE ? ++maxId : (maxId = Short.MIN_VALUE));
	}

	public EntityPassiveItem(World world, int id) {
		setEntityId(id);
		// PipeManager.getAllEntities().put(getEntityId(), this);
		worldObj = world;
	}

	public EntityPassiveItem(World world, double d, double d1, double d2) {
		this(world);
		position = new Position(d, d1, d2);
		worldObj = world;
	}

	public EntityPassiveItem(World world, double d, double d1, double d2, ItemStack itemstack) {
		this(world, d, d1, d2);
		this.setItemStack(itemstack.copy());
	}

	/* CREATING & CACHING */
	public static IPipedItem getOrCreate(World world, int id) {
		// if (PipeManager.getAllEntities().containsKey(id)) {
		// return PipeManager.getAllEntities().get(id);
		// } else {
		return new EntityPassiveItem(world, id);
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#remove()
	 */
	@Override
	public void remove() {
		// if (PipeManager.getAllEntities().containsKey(getEntityId())) {
		// PipeManager.getAllEntities().remove(getEntityId());
		// }
	}

	/* GETTING & SETTING */
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#setWorld(net.minecraft.src.World)
	 */
	@Override
	public void setWorld(World world) {
		worldObj = world;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#getPosition()
	 */
	@Override
	public Position getPosition() {
		return position;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#setPosition(double, double, double)
	 */
	@Override
	public void setPosition(double x, double y, double z) {
		position = new Position(x, y, z);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#getSpeed()
	 */
	@Override
	public float getSpeed() {
		return speed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#setSpeed(float)
	 */
	@Override
	public void setSpeed(float speed) {
		this.speed = speed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#getItemStack()
	 */
	@Override
	public ItemStack getItemStack() {
		return item;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#setItemStack(net.minecraft.src.ItemStack)
	 */
	@Override
	public void setItemStack(ItemStack item) {
		this.item = item;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#getContainer()
	 */
	@Override
	public TileEntity getContainer() {
		return container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#setContainer(net.minecraft.src.TileEntity)
	 */
	@Override
	public void setContainer(TileEntity container) {
		this.container = container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#getSynchroTracker()
	 */
	@Override
	@Deprecated
	public SafeTimeTracker getSynchroTracker() {
		return synchroTracker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#setSynchroTracker(net.minecraft.src.buildcraft.api.SafeTimeTracker)
	 */
	@Override
	@Deprecated
	public void setSynchroTracker(SafeTimeTracker synchroTracker) {
		this.synchroTracker = synchroTracker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#getDeterministicRandomization()
	 */
	@Override
	@Deprecated
	public int getDeterministicRandomization() {
		return deterministicRandomization;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#setDeterministicRandomization(int)
	 */
	@Override
	@Deprecated
	public void setDeterministicRandomization(int deterministicRandomization) {
		this.deterministicRandomization = deterministicRandomization;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#getEntityId()
	 */
	@Override
	public int getEntityId() {
		return entityId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#setEntityId(int)
	 */
	@Override
	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	/* SAVING & LOADING */
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#readFromNBT(net.minecraft.src.NBTTagCompound)
	 */
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		setPosition(nbttagcompound.getDouble("x"), nbttagcompound.getDouble("y"), nbttagcompound.getDouble("z"));

		setSpeed(nbttagcompound.getFloat("speed"));
		setItemStack(ItemStack.loadItemStackFromNBT(nbttagcompound.getCompoundTag("Item")));

		NBTTagList contribList = nbttagcompound.getTagList("contribList");

		for (int i = 0; i < contribList.tagCount(); ++i) {
			NBTTagCompound cpt = (NBTTagCompound) contribList.tagAt(i);
			String key = cpt.getString("key");

			String className = cpt.getString("class");

			if (getClass().getName().startsWith("net.minecraft.src")) {
				className = "net.minecraft.src." + className;
			}

			try {
				IPassiveItemContribution contrib = ((IPassiveItemContribution) Class.forName(className).newInstance());

				contrib.readFromNBT(cpt);

				contributions.put(key, contrib);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#writeToNBT(net.minecraft.src.NBTTagCompound)
	 */
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setDouble("x", position.x);
		nbttagcompound.setDouble("y", position.y);
		nbttagcompound.setDouble("z", position.z);
		nbttagcompound.setFloat("speed", getSpeed());
		NBTTagCompound nbttagcompound2 = new NBTTagCompound();
		getItemStack().writeToNBT(nbttagcompound2);
		nbttagcompound.setCompoundTag("Item", nbttagcompound2);

		NBTTagList contribList = new NBTTagList();

		for (String key : contributions.keySet()) {
			IPassiveItemContribution contrib = contributions.get(key);
			NBTTagCompound cpt = new NBTTagCompound();

			contrib.writeToNBT(cpt);
			cpt.setString("key", key);

			String className = contrib.getClass().getName();

			if (className.startsWith("net.minecraft.src.")) {
				className = className.replace("net.minecraft.src.", "");
			}

			cpt.setString("class", className);
			contribList.appendTag(cpt);
		}

		nbttagcompound.setTag("contribList", contribList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#toEntityItem(net.minecraft.src.buildcraft.api.Orientations)
	 */
	@Override
	public EntityItem toEntityItem(ForgeDirection dir) {
		if (!CoreProxy.proxy.isRenderWorld(worldObj)) {
			if (getItemStack().stackSize <= 0)
				return null;

			Position motion = new Position(0, 0, 0, dir);
			motion.moveForwards(0.1 + getSpeed() * 2F);

			EntityItem entityitem = new EntityItem(worldObj, position.x, position.y, position.z, getItemStack());

			entityitem.lifespan = BuildCraftCore.itemLifespan;
			entityitem.delayBeforeCanPickup = 10;

			float f3 = 0.00F + worldObj.rand.nextFloat() * 0.04F - 0.02F;
			entityitem.motionX = (float) worldObj.rand.nextGaussian() * f3 + motion.x;
			entityitem.motionY = (float) worldObj.rand.nextGaussian() * f3 + motion.y;
			entityitem.motionZ = (float) worldObj.rand.nextGaussian() * f3 + +motion.z;
			worldObj.spawnEntityInWorld(entityitem);
			remove();

			return entityitem;
		} else
			return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#getEntityBrightness(float)
	 */
	@Override
	public float getEntityBrightness(float f) {
		int i = MathHelper.floor_double(position.x);
		int j = MathHelper.floor_double(position.z);
		worldObj.getClass();
		if (worldObj.blockExists(i, 128 / 2, j)) {
			double d = 0.66000000000000003D;
			int k = MathHelper.floor_double(position.y + d);
			return worldObj.getLightBrightness(i, k, j);
		} else
			return 0.0F;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#isCorrupted()
	 */
	@Override
	public boolean isCorrupted() {
		return getItemStack() == null || getItemStack().stackSize <= 0 || Item.itemsList[getItemStack().itemID] == null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#addContribution(java.lang.String, net.minecraft.src.buildcraft.api.IPassiveItemContribution)
	 */
	@Override
	public void addContribution(String key, IPassiveItemContribution contribution) {
		contributions.put(key, contribution);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#getContribution(java.lang.String)
	 */
	@Override
	public IPassiveItemContribution getContribution(String key) {
		return contributions.get(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.buildcraft.api.IPipedItem#hasContributions()
	 */
	@Override
	public boolean hasContributions() {
		return contributions.size() > 0;
	}
}
