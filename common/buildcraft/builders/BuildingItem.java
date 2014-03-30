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
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.core.Position;
import buildcraft.core.blueprints.BuildingSlot;
import buildcraft.core.network.NetworkData;

public class BuildingItem {
	@NetworkData
	public Position origin, destination;

	@NetworkData
	public LinkedList <ItemStack> stackToBuild = new LinkedList<ItemStack>();

	public Position pos = new Position(),
			posDisplay = new Position(),
			displayDiff = new Position();

	long previousUpdate;
	boolean isDone = false;
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

			maxLifetime = size * 5.0;

			vx = dx / maxLifetime;
			vy = dy / maxLifetime;
			vz = dz / maxLifetime;

			pos.x = origin.x;
			pos.y = origin.y;
			pos.z = origin.z;

			initialized = true;
		}
	}

	public void update () {
		if (isDone) {
			return;
		}

		initialize();

		pos.x += vx;
		pos.y += vy;
		pos.z += vz;

		posDisplay.x = pos.x;
		posDisplay.y = pos.y + Math.sin(lifetime / maxLifetime * Math.PI) * 10.0;
		posDisplay.z = pos.z;

		lifetime++;

		if (lifetime > maxLifetime) {
			isDone = true;
			build ();
		}

		lifetimeDisplay = lifetime;
		displayDiff = new Position(0, 0, 0);
		previousUpdate = new Date ().getTime();
	}

	public void displayUpdate () {
		initialize();

		double tickDuration = 1000.0 / 20.0;
		long currentUpdate = new Date ().getTime();
		double timeSpan = currentUpdate - previousUpdate;
		previousUpdate = currentUpdate;

		double displayPortion = timeSpan / tickDuration;

		lifetimeDisplay += 1.0 * displayPortion;

		if (lifetimeDisplay - lifetime <= 1.0) {
			displayDiff.x += vx * displayPortion;
			displayDiff.y += vy * displayPortion;
			displayDiff.z += vz * displayPortion;

			posDisplay.x = pos.x + displayDiff.x;
			posDisplay.y = pos.y + displayDiff.y
					+ Math.sin(lifetimeDisplay / maxLifetime * Math.PI) * 10.0;
			posDisplay.z = pos.z + displayDiff.z;

		}
	}

	private void build() {
		if (slotToBuild != null) {
			slotToBuild.writeToWorld(context);
		}
	}
}
