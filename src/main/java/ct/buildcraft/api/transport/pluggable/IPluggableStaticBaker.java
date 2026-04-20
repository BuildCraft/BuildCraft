package ct.buildcraft.api.transport.pluggable;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IPluggableStaticBaker<K extends PluggableModelKey> {
    List<BakedQuad> bake(K key);
}
