package ct.buildcraft.api.transport.pipe;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;

public interface IPipeFlowBaker<F extends PipeFlow> {
    List<BakedQuad> bake(F flow);
}
