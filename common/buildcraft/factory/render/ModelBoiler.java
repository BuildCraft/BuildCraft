package buildcraft.factory.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelBoiler {

	public static final ResourceLocation TEXTURE_FRONT = new ResourceLocation("buildcraft:textures/models/boiler_front.png");
	public static final ResourceLocation TEXTURE_MIDDLE = new ResourceLocation("buildcraft:textures/models/boiler_middle.png");
	public static final ResourceLocation TEXTURE_BACK = new ResourceLocation("buildcraft:textures/models/boiler_back.png");

	public static class Front extends ModelBase {

		ModelRenderer frontPanel;

		public Front() {
			textureWidth = 128;
			textureHeight = 64;

			frontPanel = new ModelRenderer(this, 0, 0);
			frontPanel.addBox(0F, 0F, 0F, 48, 48, 16);
			frontPanel.setRotationPoint(-24F, -24F, -8F);
			frontPanel.setTextureSize(128, 64);
			frontPanel.mirror = true;
			setRotation(frontPanel, 0F, 0F, 0F);
		}

		public void render(float f5) {
			frontPanel.render(f5);
		}

		private void setRotation(ModelRenderer model, float x, float y, float z) {
			model.rotateAngleX = x;
			model.rotateAngleY = y;
			model.rotateAngleZ = z;
		}

	}


	public static class Middle extends ModelBase {

		ModelRenderer frame;
		ModelRenderer base;
		ModelRenderer tank;

		public Middle() {
			textureWidth = 128;
			textureHeight = 128;

			frame = new ModelRenderer(this, 0, 80);
			frame.addBox(0F, 0F, 0F, 47, 32, 2);
			frame.setRotationPoint(-23.5F, -23.5F, -9F);
			frame.setTextureSize(128, 128);
			frame.mirror = true;
			setRotation(frame, 0F, 0F, 0F);
			base = new ModelRenderer(this, 0, 0);
			base.addBox(0F, 0F, 0F, 48, 16, 16);
			base.setRotationPoint(-24F, 8F, -8F);
			base.setTextureSize(128, 128);
			base.mirror = true;
			setRotation(base, 0F, 0F, 0F);
			tank = new ModelRenderer(this, 0, 32);
			tank.addBox(0F, 0F, 0F, 46, 32, 16);
			tank.setRotationPoint(-23F, -23F, -8.01F);
			tank.setTextureSize(128, 128);
			tank.mirror = true;
			setRotation(tank, 0F, 0F, 0F);
		}

		public void render(float f5) {
			frame.render(f5);
			base.render(f5);
			tank.render(f5);
		}

		private void setRotation(ModelRenderer model, float x, float y, float z) {
			model.rotateAngleX = x;
			model.rotateAngleY = y;
			model.rotateAngleZ = z;
		}

	}

	public static class Back extends ModelBase {

		ModelRenderer frame;
		ModelRenderer base;
		ModelRenderer tank;

		public Back() {
			textureWidth = 128;
			textureHeight = 128;

			frame = new ModelRenderer(this, 0, 80);
			frame.addBox(0F, 0F, 0F, 47, 32, 2);
			frame.setRotationPoint(-23.5F, -23.5F, -9F);
			frame.setTextureSize(128, 128);
			frame.mirror = true;
			setRotation(frame, 0F, 0F, 0F);
			base = new ModelRenderer(this, 0, 0);
			base.addBox(0F, 0F, 0F, 48, 16, 16);
			base.setRotationPoint(-24F, 8F, -8F);
			base.setTextureSize(128, 128);
			base.mirror = true;
			setRotation(base, 0F, 0F, 0F);
			tank = new ModelRenderer(this, 0, 32);
			tank.addBox(0F, 0F, 0F, 46, 32, 16);
			tank.setRotationPoint(-23F, -23F, -9.01F);
			tank.setTextureSize(128, 128);
			tank.mirror = true;
			setRotation(tank, 0F, 0F, 0F);
		}

		public void render(float f5) {
			frame.render(f5);
			base.render(f5);
			tank.render(f5);
		}

		private void setRotation(ModelRenderer model, float x, float y, float z) {
			model.rotateAngleX = x;
			model.rotateAngleY = y;
			model.rotateAngleZ = z;
		}

	}


}
