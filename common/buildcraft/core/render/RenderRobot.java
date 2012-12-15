package buildcraft.core.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.ForgeHooksClient;

import org.lwjgl.opengl.GL11;

import buildcraft.core.DefaultProps;
import buildcraft.core.EntityRobot;

public class RenderRobot extends Render {

	protected ModelBase model = new ModelBase() {};
	private ModelRenderer box;

	public RenderRobot() {
		box = new ModelRenderer(model, 0, 0);
		box.addBox(-4F, -4F, -4F, 8, 8, 8);
		box.rotationPointX = 0;
		box.rotationPointY = 0;
		box.rotationPointZ = 0;
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float f1) {
		doRender((EntityRobot) entity, x, y, z, f, f1);
	}

	private void doRender(EntityRobot laser, double x, double y, double z, float f, float f1) {

		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);
		GL11.glTranslated(x, y, z);

		ForgeHooksClient.bindTexture(DefaultProps.TEXTURE_PATH_ENTITIES + "/robot.png", 0);

		float factor = (float) (1.0 / 16.0);

		box.render(factor);

		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();

	}

}
