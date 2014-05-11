/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import java.util.Date;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.Constants;

import buildcraft.BuildCraftBuilders;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingNotFoundException;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.core.NetworkData;
import buildcraft.api.core.Position;
import buildcraft.core.blueprints.BuildingSlot;
import buildcraft.core.blueprints.BuildingSlotBlock;
import buildcraft.core.blueprints.BuildingSlotEntity;
import buildcraft.core.blueprints.IBuilder;

public class BuildingItem implements IBuilder {

	private static int ITEMS_SPACE = 2;

	@NetworkData
	public Position origin, destination;

	@NetworkData
	public LinkedList<StackAtPosition> stacksToDisplay = new LinkedList<StackAtPosition>();

	public Position posDisplay = new Position();
	public boolean isDone = false;

	public BuildingSlot slotToBuild;
	public IBuilderContext context;

	public double receivedProgress = 0;

	private long previousUpdate;
	private double lifetimeDisplay = 0;
	private double maxLifetime = 0;
	private boolean initialized = false;
	private double vx, vy, vz;
	private double maxHeight;

	@NetworkData
	private double lifetime = 0;

	public void initialize () {
		if (!initialized) {
			double dx = destination.x - origin.x;
			double dy = destination.y - origin.y;
			double dz = destination.z - origin.z;

			double size = Math.sqrt(dx * dx + dy * dy + dz * dz);

			maxLifetime = size * 4;

			// maxHeight = 5.0 + (destination.y - origin.y) / 2.0;

			maxHeight = size / 2;

			// the below computation is an approximation of the distance to
			// travel for the object. It really follows a sinus, but we compute
			// the size of a triangle for simplification.

			Position middle = new Position();
			middle.x = (destination.x + origin.x) / 2;
			middle.y = (destination.y + origin.y) / 2;
			middle.z = (destination.z + origin.z) / 2;

			Position top = new Position ();
			top.x = middle.x;
			top.y = middle.y + maxHeight;
			top.z = middle.z;

			Position originToTop = new Position ();
			originToTop.x = top.x - origin.x;
			originToTop.y = top.y - origin.y;
			originToTop.z = top.z - origin.z;

			Position destinationToTop = new Position ();
			destinationToTop.x = destination.x - origin.x;
			destinationToTop.y = destination.y - origin.y;
			destinationToTop.z = destination.z - origin.z;

			Position distance = new Position();

			double d1 = Math.sqrt(originToTop.x * originToTop.x + originToTop.y
					* originToTop.y + originToTop.z * originToTop.z);

			double d2 = Math.sqrt(destinationToTop.x * destinationToTop.x + destinationToTop.y
					* destinationToTop.y + destinationToTop.z * destinationToTop.z);

			d1 = d1 / size * maxLifetime;
			d2 = d2 / size * maxLifetime;

			maxLifetime = d1 + d2;

			vx = dx / maxLifetime;
			vy = dy / maxLifetime;
			vz = dz / maxLifetime;

			if (stacksToDisplay.size() == 0) {
				StackAtPosition sPos = new StackAtPosition();
				sPos.stack = new ItemStack(BuildCraftBuilders.buildToolBlock);
				stacksToDisplay.add(sPos);
			}

			initialized = true;
		}
	}

	public Position getDisplayPosition (double time) {
		Position result = new Position ();

		result.x = origin.x + vx * time;
		result.y = origin.y + vy * time + Math.sin(time / maxLifetime * Math.PI) * maxHeight;
		result.z = origin.z + vz * time;

		return result;
	}

	public void update () {
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
		previousUpdate = new Date ().getTime();

		if (slotToBuild != null && lifetime > maxLifetime) {
			slotToBuild.writeCompleted(context, (lifetime - maxLifetime)
					/ (stacksToDisplay.size() * ITEMS_SPACE));
		}
	}

	public void displayUpdate () {
		initialize();

		double tickDuration = 1000.0 / 20.0;
		long currentUpdate = new Date ().getTime();
		double timeSpan = currentUpdate - previousUpdate;
		previousUpdate = currentUpdate;

		double displayPortion = timeSpan / tickDuration;

		if (lifetimeDisplay - lifetime <= 1.0) {
			lifetimeDisplay += 1.0 * displayPortion;
		}
	}

	private void build() {
		if (slotToBuild != null) {
			int destX = (int) Math.floor(destination.x);
			int destY = (int) Math.floor(destination.y);
			int destZ = (int) Math.floor(destination.z);
			Block block = context.world().getBlock(destX, destY, destZ);
			int meta = context.world().getBlockMetadata(destX, destY, destZ);

			context.world().playAuxSFXAtEntity(null, 2001,
					destX, destY, destZ,
					Block.getIdFromBlock(block) + (meta << 12));

			/*if (BlockUtil.isToughBlock(context.world(), destX, destY, destZ)) {
				BlockUtil.breakBlock(context.world(), destX, destY, destZ, BuildCraftBuilders.fillerLifespanTough);
			} else {
				BlockUtil.breakBlock(context.world(), destX, destY, destZ, BuildCraftBuilders.fillerLifespanNormal);
			}*/

			slotToBuild.writeToWorld(context);
		}
	}

	public LinkedList<StackAtPosition> getStacks() {
		int d = 0;

		for (StackAtPosition s : stacksToDisplay) {
			double stackLife = lifetimeDisplay - d;

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

	public void writeToNBT (NBTTagCompound nbt) {
		NBTTagCompound originNBT = new NBTTagCompound();
		origin.writeToNBT(originNBT);
		nbt.setTag ("origin", originNBT);

		NBTTagCompound destinationNBT = new NBTTagCompound();
		destination.writeToNBT(destinationNBT);
		nbt.setTag ("destination", destinationNBT);

		nbt.setDouble("lifetime", lifetime);

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
			nbt.setByte ("slotKind", (byte) 0);
		} else {
			nbt.setByte ("slotKind", (byte) 1);
		}

		nbt.setTag("slotToBuild", slotNBT);
	}

	public void readFromNBT(NBTTagCompound nbt) throws MappingNotFoundException {
		origin = new Position(nbt.getCompoundTag("origin"));
		destination = new Position (nbt.getCompoundTag("destination"));
		lifetime = nbt.getDouble("lifetime");

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

	public void setStacksToDisplay(LinkedList<ItemStack> stacks) {
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
}
