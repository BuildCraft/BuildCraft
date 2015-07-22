package buildcraft.api.transport.pluggable;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

import buildcraft.transport.Pipe;

public interface IPipePluggableRenderer {
    List<BakedQuad> renderPluggable(Pipe<?> pipe, PipePluggable pluggable, EnumFacing face);
}
