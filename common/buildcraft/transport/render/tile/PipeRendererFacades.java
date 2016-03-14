package buildcraft.transport.render.tile;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;

import buildcraft.api.transport.pluggable.IFacadePluggable;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.transport.PipePluggableState;

public class PipeRendererFacades {

    public static void renderPipeFacades(List<BakedQuad> quads, PipePluggableState state) {
        IFacadePluggable[] facades = new IFacadePluggable[6];
        for (int i = 0; i < 6; i++) {
            PipePluggable pluggable = state.getPluggables()[i];
            if (pluggable instanceof IFacadePluggable) {
                facades[i] = (IFacadePluggable) pluggable;
            }
        }
    }
}
