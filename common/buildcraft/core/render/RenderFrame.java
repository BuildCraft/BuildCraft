package buildcraft.core.render;

import buildcraft.core.EntityFrame;
import buildcraft.core.LaserData;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class RenderFrame extends Render {

	public RenderFrame() {

	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float f1) {
		doRender((EntityFrame) entity, x, y, z, f, f1);
	}

	private void doRender(EntityFrame frame, double x, double y, double z, float f, float f1) {
		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);
		GL11.glTranslated(x, y, z);

		for (LaserData l : frame.lasers) {
			GL11.glPushMatrix();
			GL11.glTranslated(l.head.x - frame.posX, l.head.y - frame.posY, l.head.z - frame.posZ);
			RenderLaser.doRenderLaser(renderManager.renderEngine, l, frame.getTexture());
			GL11.glPopMatrix();
		}

		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}
}
