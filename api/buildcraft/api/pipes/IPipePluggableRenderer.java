package buildcraft.api.pipes;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.render.ITextureStates;

public interface IPipePluggableRenderer {
	void renderPluggable(RenderBlocks renderblocks, IPipe pipe, ForgeDirection side,
						 IPipePluggable pipePluggable, ITextureStates blockStateMachine,
						 int x, int y, int z);
}
