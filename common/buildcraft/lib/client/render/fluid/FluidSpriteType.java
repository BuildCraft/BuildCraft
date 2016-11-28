package buildcraft.lib.client.render.fluid;

import net.minecraftforge.fluids.Fluid;

/** Determines what sprite should be used for rendering fluids. */
public enum FluidSpriteType {
    /** A completely frozen sprite - it has no animation. Useful if you need to show fluid moving around, and this stops
     * the normal fluid animation being distracting. Note that this sprite is double the size of {@link #STILL} (which
     * it is derived from), but it is just repeating (so that you don't have to calculate multiple quads for a single
     * face whenever the animation crosses over the border of the original sprite) */
    FROZEN,
    /** The sprite that {@link Fluid#getStill()} refers to. */
    STILL,
    /** The sprite that {@link Fluid#getFlowing()} refers to. */
    FLOWING;
}
