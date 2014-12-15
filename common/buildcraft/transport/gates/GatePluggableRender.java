package buildcraft.transport.gates;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.pipes.IPipe;
import buildcraft.api.pipes.IPipePluggable;
import buildcraft.api.pipes.IPipePluggableRenderer;

public class GatePluggableRender implements IPipePluggableRenderer {
	@Override
	public void renderPluggable(RenderBlocks renderblocks, IPipe pipe, ForgeDirection side, IPipePluggable pipePluggable, ITextureStates blockStateMachine, int x, int y, int z) {

	}
}
