package buildcraft.factory.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;

public class ModelTower {

	public static final ResourceLocation TEXTURE_BASE_SMALL = new ResourceLocation("buildcraft:textures/models/tower_base_small.png");
	public static final ResourceLocation TEXTURE_TOP_SMALL = new ResourceLocation("buildcraft:textures/models/tower_top_small.png");

	public static class BaseSmall extends ModelBase {

		ModelRenderer towerBase;

		public BaseSmall() {
			textureWidth = 256;
			textureHeight = 64;

			towerBase = new ModelRenderer(this, 0, 0);
			towerBase.addBox(0F, 0F, 0F, 48, 16, 48);
			towerBase.setRotationPoint(-24F, 8F, -24F);
			towerBase.setTextureSize(256, 64);
			towerBase.mirror = true;
			setRotation(towerBase, 0F, 0F, 0F);
		}

		public void render(float f5) {
			towerBase.render(f5);
		}

		private void setRotation(ModelRenderer model, float x, float y, float z) {
			model.rotateAngleX = x;
			model.rotateAngleY = y;
			model.rotateAngleZ = z;
		}

	}

	public static class TopSmall extends ModelBase {

		ModelRenderer tower;
		ModelRenderer ring;
		ModelRenderer top;

		public TopSmall() {
			textureWidth = 256;
			textureHeight = 128;

			tower = new ModelRenderer(this, 0, 50);
			tower.addBox(0F, 0F, 0F, 46, 31, 46);
			tower.setRotationPoint(-23F, -7F, -23F);
			tower.setTextureSize(256, 128);
			tower.mirror = true;
			setRotation(tower, 0F, 0F, 0F);
			ring = new ModelRenderer(this, 0, 0);
			ring.addBox(0F, 0F, 0F, 48, 2, 48);
			ring.setRotationPoint(-24F, 8F, -24F);
			ring.setTextureSize(256, 128);
			ring.mirror = true;
			setRotation(ring, 0F, 0F, 0F);
			top = new ModelRenderer(this, 0, 0);
			top.addBox(0F, 0F, 0F, 48, 2, 48);
			top.setRotationPoint(-24F, -8F, -24F);
			top.setTextureSize(256, 128);
			top.mirror = true;
			setRotation(top, 0F, 0F, 0F);
		}

		public void render(float f5) {
			tower.render(f5);
			ring.render(f5);
			top.render(f5);
		}

		private void setRotation(ModelRenderer model, float x, float y, float z) {
			model.rotateAngleX = x;
			model.rotateAngleY = y;
			model.rotateAngleZ = z;
		}

	}

}