package buildcraft.api.transport.pluggable;

import net.minecraft.util.EnumFacing;

import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.transport.IPipe;

public interface IPipePluggableRenderer {
	void renderPluggable(RenderBlocks renderblocks, IPipe pipe, EnumFacing side,
						 PipePluggable pipePluggable, ITextureStates blockStateMachine,
						 int renderPass, int x, int y, int z);
}
