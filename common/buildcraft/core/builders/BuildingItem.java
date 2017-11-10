/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.builders;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;

import net.minecraftforge.common.util.Constants;

import buildcraft.BuildCraftCore;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingNotFoundException;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.core.ISerializable;
import buildcraft.api.core.Position;
import buildcraft.core.BlockBuildTool;
import buildcraft.core.StackAtPosition;
import buildcraft.core.lib.inventory.InvUtils;

public class BuildingItem implements IBuildingItem, ISerializable {

	public static int ITEMS_SPACE = 2;

	public Position origin, destination;
	public LinkedList<StackAtPosition> stacksToDisplay = new LinkedList<StackAtPosition>();

	public boolean isDone = false;

	public BuildingSlot slotToBuild;
	public IBuilderContext context;

	private long previousUpdate;
	private float lifetimeDisplay = 0;
	private float maxLifetime = 0;
	private boolean initialized = false;
	private double vx, vy, vz;
	private double maxHeight;
	private float lifetime = 0;

	public void initialize() {
		if (!initialized) {
			double dx = destination.x - origin.x;
			double dy = destination.y - origin.y;
			double dz = destination.z - origin.z;

			double size = Math.sqrt(dx * dx + dy * dy + dz * dz);

			maxLifetime = (float) size * 4;

			// maxHeight = 5.0 + (destination.y - origin.y) / 2.0;

			maxHeight = size / 2;

			// the below computation is an approximation of the distance to
			// travel for the object. It really follows a sinus, but we compute
			// the size of a triangle for simplification.

			Position middle = new Position();
			middle.x = (destination.x + origin.x) / 2;
			middle.y = (destination.y + origin.y) / 2;
			middle.z = (destination.z + origin.z) / 2;

			Position top = new Position();
			top.x = middle.x;
			top.y = middle.y + maxHeight;
			top.z = middle.z;

			Position originToTop = new Position();
			originToTop.x = top.x - origin.x;
			originToTop.y = top.y - origin.y;
			originToTop.z = top.z - origin.z;

			Position destinationToTop = new Position();
			destinationToTop.x = destination.x - origin.x;
			destinationToTop.y = destination.y - origin.y;
			destinationToTop.z = destination.z - origin.z;

			double d1 = Math.sqrt(originToTop.x * originToTop.x + originToTop.y
					* originToTop.y + originToTop.z * originToTop.z);

			double d2 = Math.sqrt(destinationToTop.x * destinationToTop.x + destinationToTop.y
					* destinationToTop.y + destinationToTop.z * destinationToTop.z);

			d1 = d1 / size * maxLifetime;
			d2 = d2 / size * maxLifetime;

			maxLifetime = (float) d1 + (float) d2;

			vx = dx / maxLifetime;
			vy = dy / maxLifetime;
			vz = dz / maxLifetime;

			if (stacksToDisplay.size() == 0) {
				StackAtPosition sPos = new StackAtPosition();
				sPos.stack = new ItemStack(BuildCraftCore.buildToolBlock);
				stacksToDisplay.add(sPos);
			}

			initialized = true;
		}
	}

	public Position getDisplayPosition(float time) {
		Position result = new Position();

		result.x = origin.x + vx * time;
		result.y = origin.y + vy * time + MathHelper.sin(time / maxLifetime * (float) Math.PI) * maxHeight;
		result.z = origin.z + vz * time;

		return result;
	}

	public void update() {
		if (isDone) {
			return;
		}

		initialize();

		lifetime++;

		if (lifetime > maxLifetime + stacksToDisplay.size() * ITEMS_SPACE - 1) {
			isDone = true;
			build();
		}

		lifetimeDisplay = lifetime;
		previousUpdate = new Date().getTime();

		if (slotToBuild != null && lifetime > maxLifetime) {
			slotToBuild.writeCompleted(context, (lifetime - maxLifetime)
					/ (stacksToDisplay.size() * ITEMS_SPACE));
		}
	}

	public void displayUpdate() {
		initialize();

		float tickDuration = 50.0F; // miliseconds
		long currentUpdate = new Date().getTime();
		float timeSpan = currentUpdate - previousUpdate;
		previousUpdate = currentUpdate;

		float displayPortion = timeSpan / tickDuration;

		if (lifetimeDisplay - lifetime <= 1.0) {
			lifetimeDisplay += 1.0 * displayPortion;
		}
	}

	private void build() {
		if (slotToBuild != null) {
			/*if (BlockUtil.isToughBlock(context.world(), destX, destY, destZ)) {
				BlockUtil.breakBlock(context.world(), destX, destY, destZ, BuildCraftBuilders.fillerLifespanTough);
			} else {
				BlockUtil.breakBlock(context.world(), destX, destY, destZ, BuildCraftBuilders.fillerLifespanNormal);
			}*/

			int destX = (int) Math.floor(destination.x);
			int destY = (int) Math.floor(destination.y);
			int destZ = (int) Math.floor(destination.z);
			Block oldBlock = context.world().getBlock(destX, destY, destZ);
			int oldMeta = context.world().getBlockMetadata(destX, destY, destZ);

			if (slotToBuild.writeToWorld(context)) {
				context.world().playAuxSFXAtEntity(null, 2001,
						destX, destY, destZ,
						Block.getIdFromBlock(oldBlock) + (oldMeta << 12));
			} else if (slotToBuild.stackConsumed != null) {
				for (ItemStack s : slotToBuild.stackConsumed) {
					if (s != null && !(s.getItem() instanceof ItemBlock && Block.getBlockFromItem(s.getItem()) instanceof BlockBuildTool)) {
						InvUtils.dropItems(context.world(), s, destX, destY, destZ);
					}
				}
			}
		}
	}

	public LinkedList<StackAtPosition> getStacks() {
		int d = 0;

		for (StackAtPosition s : stacksToDisplay) {
			float stackLife = lifetimeDisplay - d;

			if (stackLife <= maxLifetime && stackLife > 0) {
				s.pos = getDisplayPosition(stackLife);
				s.display = true;
			} else {
				s.display = false;
			}

			d += ITEMS_SPACE;
		}

		return stacksToDisplay;
	}

	@Override
	public boolean isDone() {
		return isDone;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagCompound originNBT = new NBTTagCompound();
		origin.writeToNBT(originNBT);
		nbt.setTag("origin", originNBT);

		NBTTagCompound destinationNBT = new NBTTagCompound();
		destination.writeToNBT(destinationNBT);
		nbt.setTag("destination", destinationNBT);

		nbt.setFloat("lifetime", lifetime);

		NBTTagList items = new NBTTagList();

		for (StackAtPosition s : stacksToDisplay) {
			NBTTagCompound cpt = new NBTTagCompound();
			s.stack.writeToNBT(cpt);
			items.appendTag(cpt);
		}

		nbt.setTag("items", items);

		MappingRegistry registry = new MappingRegistry();

		NBTTagCompound slotNBT = new NBTTagCompound();
		NBTTagCompound registryNBT = new NBTTagCompound();

		slotToBuild.writeToNBT(slotNBT, registry);
		registry.write(registryNBT);

		nbt.setTag("registry", registryNBT);

		if (slotToBuild instanceof BuildingSlotBlock) {
			nbt.setByte("slotKind", (byte) 0);
		} else {
			nbt.setByte("slotKind", (byte) 1);
		}

		nbt.setTag("slotToBuild", slotNBT);
	}

	public void readFromNBT(NBTTagCompound nbt) throws MappingNotFoundException {
		origin = new Position(nbt.getCompoundTag("origin"));
		destination = new Position(nbt.getCompoundTag("destination"));
		lifetime = nbt.getFloat("lifetime");

		NBTTagList items = nbt.getTagList("items",
				Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < items.tagCount(); ++i) {
			StackAtPosition sPos = new StackAtPosition();
			sPos.stack = ItemStack.loadItemStackFromNBT(items
					.getCompoundTagAt(i));
			stacksToDisplay.add(sPos);
		}

		MappingRegistry registry = new MappingRegistry();
		registry.read(nbt.getCompoundTag("registry"));

		if (nbt.getByte("slotKind") == 0) {
			slotToBuild = new BuildingSlotBlock();
		} else {
			slotToBuild = new BuildingSlotEntity();
		}

		slotToBuild.readFromNBT(nbt.getCompoundTag("slotToBuild"), registry);
	}

	public void setStacksToDisplay(List<ItemStack> stacks) {
		if (stacks != null) {
			for (ItemStack s : stacks) {
				for (int i = 0; i < s.stackSize; ++i) {
					StackAtPosition sPos = new StackAtPosition();
					sPos.stack = s.copy();
					sPos.stack.stackSize = 1;
					stacksToDisplay.add(sPos);
				}
			}
		}
	}

	@Override
	public void readData(ByteBuf stream) {
		origin = new Position();
		destination = new Position();
		origin.readData(stream);
		destination.readData(stream);
		lifetime = stream.readFloat();
		stacksToDisplay.clear();
		int size = stream.readUnsignedShort();
		for (int i = 0; i < size; i++) {
			StackAtPosition e = new StackAtPosition();
			e.readData(stream);
			stacksToDisplay.add(e);
		}
	}

	@Override
	public void writeData(ByteBuf stream) {
		origin.writeData(stream);
		destination.writeData(stream);
		stream.writeFloat(lifetime);
		stream.writeShort(stacksToDisplay.size());
		for (StackAtPosition s : stacksToDisplay) {
			s.writeData(stream);
		}
	}

	@Override
	public int hashCode() {
		return (131 * origin.hashCode()) + destination.hashCode();
	}
}
