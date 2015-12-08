package buildcraft.transport.utils;

import net.minecraftforge.fluids.FluidStack;

public class FluidRenderData {
	public int fluidID, color, flags;
    public int[] amount = new int[7];
    public byte[] flow = new byte[6];

    public FluidRenderData duplicate() {
        FluidRenderData n = new FluidRenderData();
        n.fluidID = fluidID;
        n.color = color;
        System.arraycopy(amount, 0, n.amount, 0, 7);
        System.arraycopy(flow, 0, n.flow, 0, 6);
		n.flags = flags;
        return n;
    }

	public static int getFlags(FluidStack s) {
		if (s == null) {
			return 0;
		}
		return s.getFluid().getLuminosity(s);
	}
}
