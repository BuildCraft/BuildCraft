/*
 * Copyright (c) SpaceToad, 2011-2012
 * http://www.mod-buildcraft.com
 * 
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes.events;

import buildcraft.transport.TravelingItem;
import java.util.List;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

/**
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public abstract class PipeEventItem extends PipeEvent {

	public final TravelingItem item;

	public PipeEventItem(TravelingItem item) {
		this.item = item;
	}

	public static class Entered extends PipeEventItem {

		public boolean cancelled = false;

		public Entered(TravelingItem item) {
			super(item);
		}
	}

	public static class ReachedCenter extends PipeEventItem {

		public ReachedCenter(TravelingItem item) {
			super(item);
		}
	}

	public static class ReachedEnd extends PipeEventItem {

		public final TileEntity dest;
		public boolean handled = false;

		public ReachedEnd(TravelingItem item, TileEntity dest) {
			super(item);
			this.dest = dest;
		}
	}

	public static class DropItem extends PipeEventItem {

		public EntityItem entity;

		public DropItem(TravelingItem item, EntityItem entity) {
			super(item);
			this.entity = entity;
		}
	}

	public static class FindDest extends PipeEventItem {

		public final List<ForgeDirection> destinations;

		public FindDest(TravelingItem item, List<ForgeDirection> destinations) {
			super(item);
			this.destinations = destinations;
		}
	}

	public static class AdjustSpeed extends PipeEventItem {

		public boolean handled = false;

		public AdjustSpeed(TravelingItem item) {
			super(item);
		}
	}
}
