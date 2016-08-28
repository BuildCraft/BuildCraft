package buildcraft.lib.client.render.fluid;

import net.minecraftforge.fluids.Fluid;

/** Determines what sprite should be used for rendering fluids. */
public enum FluidSpriteType {
    /** A completely frozen sprite - it has no animation. Useful if you need to show fluid moving around, and this stops
     * the animation from distracting that. */
    FROZEN,
    /** The sprite that {@link Fluid#getStill()} refers to. */
    STILL,
    /** The sprite that {@link Fluid#getFlowing()} refers to. */
    FLOWING;
}
