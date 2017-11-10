/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes.events;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.transport.Pipe;
import buildcraft.transport.TransportConstants;
import buildcraft.transport.TravelingItem;

public abstract class PipeEventItem extends PipeEvent {

	public final TravelingItem item;

	public PipeEventItem(Pipe<?> pipe, TravelingItem item) {
		super(pipe);
		this.item = item;
	}

	public static class Entered extends PipeEventItem {
		public boolean cancelled = false;

		public Entered(Pipe<?> pipe, TravelingItem item) {
			super(pipe, item);
		}
	}

	public static class ReachedCenter extends PipeEventItem {
		public ReachedCenter(Pipe<?> pipe, TravelingItem item) {
			super(pipe, item);
		}
	}

	public static class ReachedEnd extends PipeEventItem {
		public final TileEntity dest;
		public boolean handled = false;

		public ReachedEnd(Pipe<?> pipe, TravelingItem item, TileEntity dest) {
			super(pipe, item);
			this.dest = dest;
		}
	}

	public static class DropItem extends PipeEventItem {
		public EntityItem entity;
		public ForgeDirection direction;

		public DropItem(Pipe<?> pipe, TravelingItem item, EntityItem entity) {
			super(pipe, item);
			this.entity = entity;
			this.direction = item.output != ForgeDirection.UNKNOWN ? item.output : item.input;
		}
	}

	public static class FindDest extends PipeEventItem {
		public final List<ForgeDirection> destinations;
		public boolean shuffle = true;

		public FindDest(Pipe<?> pipe, TravelingItem item, List<ForgeDirection> destinations) {
			super(pipe, item);
			this.destinations = destinations;
		}
	}

	public static class AdjustSpeed extends PipeEventItem {
		public boolean handled = false;
		public float slowdownAmount = TransportConstants.PIPE_SLOWDOWN_SPEED;

		public AdjustSpeed(Pipe<?> pipe, TravelingItem item) {
			super(pipe, item);
		}
	}
}
