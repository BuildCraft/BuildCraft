package buildcraft.transport.pipes.events;

import net.minecraftforge.common.util.ForgeDirection;

public abstract class PipeEventPower extends PipeEvent {
	public final ForgeDirection from;
	/**
	 * The amount of power left after processing.
	 */
	public int power;

	public PipeEventPower(ForgeDirection from, int power) {
		this.from = from;
		this.power = power;
	}

	public static class Request extends PipeEventPower {
		public Request(ForgeDirection from, int power) {
			super(from, power);
		}
	}

	public static class Receive extends PipeEventPower {
		public boolean override;

		public Receive(ForgeDirection from, int power) {
			super(from, power);
			this.override = false;
		}
	}
}
