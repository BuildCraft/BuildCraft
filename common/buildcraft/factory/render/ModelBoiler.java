package buildcraft.factory.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelBoiler extends ModelBase {

	ModelRenderer boilerTank;
	ModelRenderer boilerPanel;
	ModelRenderer boilerBase;

	public ModelBoiler() {
		textureWidth = 256;
		textureHeight = 256;

		boilerTank = new ModelRenderer(this, 0, 0);
		boilerTank.addBox(0F, 0F, 0F, 46, 32, 32);
		boilerTank.setRotationPoint(-23F, -23F, -9F);
		boilerTank.setTextureSize(256, 256);
		boilerTank.mirror = true;
		setRotation(boilerTank, 0F, 0F, 0F);
		boilerPanel = new ModelRenderer(this, 0, 128);
		boilerPanel.addBox(0F, 0F, 0F, 48, 32, 16);
		boilerPanel.setRotationPoint(-24F, -24F, -24F);
		boilerPanel.setTextureSize(256, 256);
		boilerPanel.mirror = true;
		setRotation(boilerPanel, 0F, 0F, 0F);
		boilerBase = new ModelRenderer(this, 0, 64);
		boilerBase.addBox(0F, 0F, 0F, 48, 16, 48);
		boilerBase.setRotationPoint(-24F, 8F, -24F);
		boilerBase.setTextureSize(256, 256);
		boilerBase.mirror = true;
		setRotation(boilerBase, 0F, 0F, 0F);
	}

	public void render(float f5) {
		boilerTank.render(f5);
		boilerPanel.render(f5);
		boilerBase.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

}
