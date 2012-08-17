package buildcraft.core.render;

import net.minecraft.src.Entity;
import net.minecraft.src.ModelBase;
import net.minecraft.src.ModelRenderer;
import net.minecraft.src.Render;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;

import buildcraft.core.EntityLaser;

public class RenderLaser extends Render {

	protected ModelBase model = new ModelBase() {};
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
		
		if (!laser.isVisible())
			return;

		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);
		GL11.glTranslated(x, y, z);

		GL11.glRotatef((float) laser.angleZ, 0, 1, 0);
		GL11.glRotatef((float) laser.angleY, 0, 0, 1);

		ForgeHooksClient.bindTexture(laser.getTexture(), 0);

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

		GL11.glPopMatrix();

	}

	protected void iterate(EntityLaser laser) {

	}

	protected ModelRenderer getBox(EntityLaser laser) {
		return box;
	}

}
