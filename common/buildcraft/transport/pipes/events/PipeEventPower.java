package buildcraft.transport.pipes.events;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.transport.Pipe;

public abstract class PipeEventPower extends PipeEvent {
	public final ForgeDirection from;
	/**
	 * The amount of power left after processing.
	 */
	public int power;

	public PipeEventPower(Pipe<?> pipe, ForgeDirection from, int power) {
		super(pipe);
		this.from = from;
		this.power = power;
	}

	public static class Request extends PipeEventPower {
		public Request(Pipe<?> pipe, ForgeDirection from, int power) {
			super(pipe, from, power);
		}
	}

	public static class Receive extends PipeEventPower {
		public boolean override;

		public Receive(Pipe<?> pipe, ForgeDirection from, int power) {
			super(pipe, from, power);
			this.override = false;
		}
	}
}
