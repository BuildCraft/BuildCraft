/*
 * This code is the property of CovertJaguar
 * and may only be used with explicit written
 * permission unless otherwise specified on the
 * license page at railcraft.wikispaces.com.
 */
package buildcraft.transport.pipes;

import buildcraft.transport.PipeTransportLiquids;

/**
 *
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public class PipeLiquidsEmerald extends PipeLiquidsWood {

	public PipeLiquidsEmerald(int itemID) {
		super(new PipeLogicEmerald(), itemID);

		baseTexture = 6 * 16 + 15;
		plainTexture = baseTexture - 1;

		((PipeTransportLiquids) transport).flowRate = 40;
		((PipeTransportLiquids) transport).travelDelay = 4;
	}
}
