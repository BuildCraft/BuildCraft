package buildcraft.lib.debug;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.render.DetatchedRenderer.IDetachedRenderer;

public interface IAdvDebugTarget {
    void disableDebugging();

    boolean doesExistInWorld();

    void sendDebugState();

    @SideOnly(Side.CLIENT)
    IDetachedRenderer getDebugRenderer();
}
