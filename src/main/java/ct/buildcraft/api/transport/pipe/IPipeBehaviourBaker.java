package ct.buildcraft.api.transport.pipe;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;

public interface IPipeBehaviourBaker<B extends PipeBehaviour> {
    List<BakedQuad> bake(B behaviour);
}
