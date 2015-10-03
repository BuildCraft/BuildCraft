package buildcraft.transport.utils;

public class FluidRenderData {
    public int fluidID, color;
    /** Ordinals 0 through 5 are the normal EnumFacing.Values(), ordinal 6 is for the center bit of pipe */
    public int[] amount = new int[7];

    public FluidRenderData duplicate() {
        FluidRenderData n = new FluidRenderData();
        n.fluidID = fluidID;
        n.color = color;
        System.arraycopy(this.amount, 0, n.amount, 0, 7);
        return n;
    }
}
