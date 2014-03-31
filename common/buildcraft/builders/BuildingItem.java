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

	public BuildingSlot slotToBuild;
	public IBuilderContext context;

	public void initialize () {
		if (!initialized) {
			double dx = destination.x - origin.x;
			double dy = destination.y - origin.y;
			double dz = destination.z - origin.z;

			double size = Math.sqrt(dx * dx + dy * dy + dz * dz);

			maxLifetime = size * 7.0;

			vx = dx / maxLifetime;
			vy = dy / maxLifetime;
			vz = dz / maxLifetime;

			for (ItemStack s : stacksToBuild) {
				StackAtPosition sPos = new StackAtPosition();
				sPos.stack = s;
				stacksToDisplay.add(sPos);
			}

			if (stacksToDisplay.size() == 0) {
				StackAtPosition sPos = new StackAtPosition();
				sPos.stack = new ItemStack(BuildCraftBuilders.stripesBlock);
				stacksToDisplay.add(sPos);
			}

			initialized = true;
		}
	}

	public Position getDisplayPosition (double time) {
		Position result = new Position ();

		result.x = origin.x + vx * time;
		result.y = origin.y + vy * time + Math.sin(time / maxLifetime * Math.PI) * (5.0 + (destination.y - origin.y) / 2.0);
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
