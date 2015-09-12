/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes.events;

import buildcraft.transport.Pipe;

public abstract class PipeEvent {
	public final Pipe<?> pipe;

	public PipeEvent(Pipe<?> pipe) {
		this.pipe = pipe;
	}
}
