package buildcraft.core.render;

import buildcraft.api.core.Position;
import buildcraft.core.EntityLaser;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderLaser extends Render {

	protected ModelBase model = new ModelBase() {
	};
	private ModelRenderer box;

	public RenderLaser() {

		box = new ModelRenderer(model, 0, 0);
		box.addBox(0, -0.5F, -0.5F, 16, 1, 1);
		box.rotationPointX = 0;
		box.rotationPointY = 0;
		box.rotationPointZ = 0;
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float f1) {

		doRender((EntityLaser) entity, x, y, z, f, f1);
		entity.setAngles(45, 180);
	}

	private void doRender(EntityLaser laser, double x, double y, double z, float f, float f1) {

		if (!laser.isVisible() || laser.getTexture() == null)
			return;

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glDisable(GL11.GL_LIGHTING);

		Position offset = laser.renderOffset();
		GL11.glTranslated(x + offset.x, y + offset.y, z + offset.z);

		GL11.glRotatef((float) laser.angleZ, 0, 1, 0);
		GL11.glRotatef((float) laser.angleY, 0, 0, 1);

		renderManager.renderEngine.bindTexture(laser.getTexture());

		float factor = (float) (1.0 / 16.0);

		float lasti = 0;

		for (float i = 0; i <= laser.renderSize - 1; ++i) {
			getBox(laser).render(factor);
			GL11.glTranslated(1, 0, 0);
			lasti = i;
		}

		lasti++;

		GL11.glScalef(((float) laser.renderSize - lasti), 1, 1);
		getBox(laser).render(factor);

		iterate(laser);

		GL11.glPopAttrib();
		GL11.glPopMatrix();

	}

	protected void iterate(EntityLaser laser) {
	}

	protected ModelRenderer getBox(EntityLaser laser) {
		return box;
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return ((EntityLaser) entity).getTexture();
	}
}
