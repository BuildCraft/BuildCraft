package buildcraft.api.transport.pluggable;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.IPipe;

public interface IPipePluggableRenderer {
    List<BakedQuad> renderPluggable(IPipe pipe, PipePluggable pluggable, EnumFacing face);
}
