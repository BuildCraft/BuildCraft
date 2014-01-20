package buildcraft.core.render;

import buildcraft.core.DefaultProps;
import buildcraft.core.EntityLaser;
import buildcraft.core.EntityRobot;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class RenderRobot extends Render {

	public static final ResourceLocation TEXTURE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/robot.png");
	protected ModelBase model = new ModelBase() {
	};
	private ModelRenderer box;
	private ModelRenderer laserBox;

	public RenderRobot() {
		box = new ModelRenderer(model, 0, 0);
		box.addBox(-4F, -4F, -4F, 8, 8, 8);
		box.rotationPointX = 0;
		box.rotationPointY = 0;
		box.rotationPointZ = 0;

		laserBox = new ModelRenderer(model, 0, 0);
		laserBox.addBox(0, -0.5F, -0.5F, 16, 1, 1);
		laserBox.rotationPointX = 0;
		laserBox.rotationPointY = 0;
		laserBox.rotationPointZ = 0;
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float f1) {
		doRender((EntityRobot) entity, x, y, z, f, f1);
	}

	private void doRender(EntityRobot robot, double x, double y, double z, float f, float f1) {
		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);
		GL11.glTranslated(x, y, z);

		renderManager.renderEngine.bindTexture(TEXTURE);

		float factor = (float) (1.0 / 16.0);

		box.render(factor);

		if (robot.laser.isVisible) {
			robot.laser.head.x = robot.posX;
			robot.laser.head.y = robot.posY;
			robot.laser.head.z = robot.posZ;

			RenderLaser.doRenderLaser(renderManager, laserBox, robot.laser, EntityLaser.LASER_TEXTURES [1]);
		}

		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();

	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return TEXTURE;
	}
}
