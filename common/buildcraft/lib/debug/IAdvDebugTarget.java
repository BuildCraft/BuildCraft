package buildcraft.lib.debug;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.render.DetatchedRenderer.IDetachedRenderer;

/** Something that can be put into an "advanced debug" state - every tick {@link #sendDebugState()} will be called on
 * the server, to allow the client to render all of the details normally hidden on the server. */
public interface IAdvDebugTarget {
    /** Called when the current debug target changes from this to something else (or to nothing). This should inform the
     * client that it is no longer being debugged. */
    void disableDebugging();

    /** Called every tick on the server to see if this still exists in the world. If this returns false then
     * {@link #disableDebugging()} will be called, and the current debug target will be removed. */
    boolean doesExistInWorld();

    /** Called every tick on the server to send all the debug information to the client. */
    void sendDebugState();

    /** Called on the client to actually render off the target. Note that this might not be called every frame this this
     * is rendered, so the returned render should always correctly render the current debug target, provided that
     * {@link #doesExistInWorld()} returns true. */
    @SideOnly(Side.CLIENT)
    IDetachedRenderer getDebugRenderer();
}
