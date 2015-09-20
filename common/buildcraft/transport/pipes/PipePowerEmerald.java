package buildcraft.transport.pipes;

import net.minecraft.item.Item;

import buildcraft.transport.PipeIconProvider;

public class PipePowerEmerald extends PipePowerWood {
	public PipePowerEmerald(Item item) {
		super(item);

		standardIconIndex = PipeIconProvider.TYPE.PipePowerEmerald_Standard.ordinal();
		solidIconIndex = PipeIconProvider.TYPE.PipeAllEmerald_Solid.ordinal();

		transport.initFromPipe(this.getClass());
	}
}
