package buildcraft.energy.render;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import buildcraft.energy.BlockEngine;
import buildcraft.energy.TileEngine;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public class BlockHighlightHandler{

	@SubscribeEvent
	public void handleBlockHighlight(DrawBlockHighlightEvent e){
		if (e.target.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK){
			TileEntity tile = e.player.worldObj.getTileEntity(e.target.blockX, e.target.blockY, e.target.blockZ);
			if (tile instanceof TileEngine) {
				AxisAlignedBB[] aabbs = BlockEngine.boxes[((TileEngine)tile).orientation.ordinal()];
				Vec3 pos = e.player.getPosition(e.partialTicks);
				GL11.glEnable(GL11.GL_BLEND);
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
				GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
				GL11.glLineWidth(2.0F);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glDepthMask(false);
				for (AxisAlignedBB aabb : aabbs) {
					RenderGlobal.drawOutlinedBoundingBox(aabb.copy().expand(0.02, 0.02, 0.02)
						.offset(tile.xCoord, tile.yCoord, tile.zCoord)
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
