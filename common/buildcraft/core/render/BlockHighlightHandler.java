package buildcraft.core.render;

import buildcraft.core.ICustomHighlight;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import org.lwjgl.opengl.GL11;

public class BlockHighlightHandler {

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void handleBlockHighlight(DrawBlockHighlightEvent e) {
		if (e.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
			int x = e.target.blockX;
			int y = e.target.blockY;
			int z = e.target.blockZ;
			Block block = e.player.worldObj.getBlock(x, y, z);
			if (block instanceof ICustomHighlight) {
				AxisAlignedBB[] aabbs = ((ICustomHighlight) block).getBoxes(e.player.worldObj, x, y, z, e.player);
				Vec3 pos = e.player.getPosition(e.partialTicks);
				GL11.glEnable(GL11.GL_BLEND);
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
				GL11.glLineWidth(2.0F);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glDepthMask(false);
				double exp = ((ICustomHighlight) block).getExpansion();
				for (AxisAlignedBB aabb : aabbs) {
					RenderGlobal.drawOutlinedBoundingBox(aabb.copy().expand(exp, exp, exp)
							.offset(x, y, z)
							.offset(-pos.xCoord, -pos.yCoord, -pos.zCoord), -1);
				}
				GL11.glDepthMask(true);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glDisable(GL11.GL_BLEND);
				e.setCanceled(true);
			}
		}
	}
}
