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
import buildcraft.BuildCraftBuilders;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.core.Position;
import buildcraft.core.blueprints.BuildingSlot;
import buildcraft.core.blueprints.IBuilder;
import buildcraft.core.network.NetworkData;

public class BuildingItem implements IBuilder {

	@NetworkData
	public Position origin, destination;

	@NetworkData
	public LinkedList <ItemStack> stacksToBuild = new LinkedList<ItemStack>();

	public LinkedList <StackAtPosition> stacksToDisplay = new LinkedList<StackAtPosition>();

	public class StackAtPosition {
		public Position pos;
		public ItemStack stack;
		public boolean display;
	}

	public Position posDisplay = new Position();
	public boolean isDone = false;

	long previousUpdate;
	double lifetime = 0;
	double lifetimeDisplay = 0;
	double maxLifetime = 0;
	private boolean initialized = false;
	double vx, vy, vz;
	double maxHeight;

	public BuildingSlot slotToBuild;
	public IBuilderContext context;

	public double receivedProgress = 0;

	public void initialize () {
		if (!initialized) {
			double dx = destination.x - origin.x;
			double dy = destination.y - origin.y;
			double dz = destination.z - origin.z;

			double size = Math.sqrt(dx * dx + dy * dy + dz * dz);

			maxLifetime = size * 5.0;

			maxHeight = (5.0 + (destination.y - origin.y) / 2.0);

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

			if (stacksToBuild == null) {
				stacksToBuild = new LinkedList<ItemStack>();
			}

			for (ItemStack s : stacksToBuild) {
				StackAtPosition sPos = new StackAtPosition();
				sPos.stack = s;
				stacksToDisplay.add(sPos);
			}

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

		if (lifetime > maxLifetime + stacksToBuild.size() - 1) {
			isDone = true;
			build ();
		}

		lifetimeDisplay = lifetime;
		previousUpdate = new Date ().getTime();

		if (slotToBuild != null && lifetime > maxLifetime) {
			slotToBuild.writeCompleted(context, (lifetime - maxLifetime)
					/ stacksToBuild.size());
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
			Block block = context.world().getBlock((int) destination.x, (int)destination.y, (int)destination.z);
			int meta = context.world().getBlockMetadata((int) destination.x, (int)destination.y, (int)destination.z);

			context.world().playAuxSFXAtEntity(null, 2001,
					(int) destination.x, (int) destination.y,
					(int) destination.z,
					Block.getIdFromBlock(block) + (meta << 12));

			slotToBuild.writeToWorld(context);
		}
	}

	public LinkedList <StackAtPosition> getStacks () {
		int d = 0;

		for (StackAtPosition s : stacksToDisplay) {
			double stackLife = lifetimeDisplay - d;

			if (stackLife <= maxLifetime && stackLife > 0) {
				s.pos = getDisplayPosition(stackLife);
				s.display = true;
			} else {
				s.display = false;
			}

			d++;
		}

		return stacksToDisplay;
	}

	@Override
	public boolean isDone() {
		return isDone;
	}
}
