package ct.buildcraft.transport.tile;

import ct.buildcraft.transport.client.model.ModelPipe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

@OnlyIn(Dist.CLIENT)
public class TilePipeHolderModelData {
    public static ModelData build(TilePipeHolder tile) {
        return ModelData.builder().with(ModelPipe.PipeTypeModelKey, tile).build();
    }
}
