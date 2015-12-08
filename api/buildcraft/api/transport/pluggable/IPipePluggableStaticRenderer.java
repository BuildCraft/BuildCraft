package buildcraft.api.transport.pluggable;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.IPipe;

public interface IPipePluggableStaticRenderer {
    List<BakedQuad> renderStaticPluggable(IPipeRenderState render, IPipePluggableState pluggableState, IPipe pipe, PipePluggable pluggable,
            EnumFacing face);
}
