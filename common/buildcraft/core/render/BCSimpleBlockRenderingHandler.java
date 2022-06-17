package buildcraft.core.render;

import buildcraft.BuildCraftCore;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.client.renderer.Tessellator;

public abstract class BCSimpleBlockRenderingHandler implements ISimpleBlockRenderingHandler {
	protected void fixEmptyAlphaPass(int x, int y, int z) {
		if (BuildCraftCore.alphaPassBugPresent) {
			Tessellator.instance.addVertexWithUV(x, y, z, 0, 0);
			Tessellator.instance.addVertexWithUV(x, y, z, 0, 0);
			Tessellator.instance.addVertexWithUV(x, y, z, 0, 0);
			Tessellator.instance.addVertexWithUV(x, y, z, 0, 0);
		}
	}
}
