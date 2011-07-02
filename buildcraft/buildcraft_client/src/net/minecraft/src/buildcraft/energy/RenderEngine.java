package net.minecraft.src.buildcraft.energy;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Entity;
import net.minecraft.src.ModLoader;
import net.minecraft.src.ModelRenderer;
import net.minecraft.src.Render;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.core.DirectModelRenderer;
import net.minecraft.src.buildcraft.core.IInventoryRenderer;
import net.minecraft.src.buildcraft.energy.EntityEngine.EnergyStage;

public class RenderEngine extends Render implements IInventoryRenderer {

	//variables init:
	public ModelRenderer box;
	public ModelRenderer trunk;
	private String baseTexture;	

	public RenderEngine () {

		//constructor:
		box = new ModelRenderer(0, 0);
		box.addBox(-8F, -8F, -8F, 16, 4, 16);		
		box.rotationPointX = 8;
		box.rotationPointY = 8;
		box.rotationPointZ = 8;

		trunk = new ModelRenderer(0, 0);
		trunk.addBox(-4F, -4F, -4F, 8, 12, 8);
		trunk.rotationPointX = 8F;
		trunk.rotationPointY = 8F;
		trunk.rotationPointZ = 8F;
	}
	
	public RenderEngine (String baseTexture) {
		this ();
		this.baseTexture = baseTexture;
	}

	public void inventoryRender(double x, double y, double z,
			float f, float f1) {
		render(EnergyStage.Blue, 0.25F, Orientations.YPos,
				baseTexture, x, y, z);
	}
	
	@Override
	public void doRender(Entity entity, double x, double y, double z,
			float f, float f1) {				

		EntityEngine engine = (EntityEngine) entity;
		
		render(engine.getEnergyStage(), engine.progress, engine.orientation,
				engine.getTextureFile(), x, y, z);
	}
	
	private void render(EnergyStage energy, float progress,
			Orientations orientation, String baseTexture, double x, double y, double z) {
			
		GL11.glPushMatrix();

		GL11.glTranslatef((float)x, (float)y, (float)z);
		
		DirectModelRenderer movingBox = new DirectModelRenderer(0, 0);
		
		float step;		
		
		if (progress > 0.5) {
			step = 7.99F - (progress - 0.5F) * 2F * 7.99F;
		} else {
			step = progress * 2F * 7.99F;
		}

		
		movingBox.addBox(-8F, -4 + (float) step, -8F, 16, 4, 16);
		movingBox.rotationPointX = 8F;
		movingBox.rotationPointY = 8F;
		movingBox.rotationPointZ = 8F;
		
		float [] angle = {0, 0, 0};

		switch (orientation) {
		case XPos:
			angle [2] = (float) -Math.PI / 2;
			break;
		case XNeg:
			angle [2] = (float) Math.PI / 2;
			break;
		case YPos:
			break;
		case YNeg:
			angle [2] = (float) Math.PI;
			break;
		case ZPos:
			angle [0] = (float) Math.PI / 2;
			break;
		case ZNeg:
			angle [0] = (float) -Math.PI / 2;
			break;


		}
		
		box.rotateAngleX = angle [0];
		box.rotateAngleY = angle [1];
		box.rotateAngleZ = angle [2];		
		
		trunk.rotateAngleX = angle [0];
		trunk.rotateAngleY = angle [1];
		trunk.rotateAngleZ = angle [2];
		
		movingBox.rotateAngleX = angle [0];
		movingBox.rotateAngleY = angle [1];
		movingBox.rotateAngleZ = angle [2];
		
		float factor = (float) (1.0 / 16.0);
				
		GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, ModLoader
				.getMinecraftInstance().renderEngine
				.getTexture(baseTexture));		

		box.render(factor);
		movingBox.render(factor);
		

		String texture = "";

		switch (energy) {
		case Blue:
			texture = "/net/minecraft/src/buildcraft/energy/gui/trunk_blue.png";
			break;
		case Green:
			texture = "/net/minecraft/src/buildcraft/energy/gui/trunk_green.png";
			break;
		case Yellow:
			texture = "/net/minecraft/src/buildcraft/energy/gui/trunk_yellow.png";
			break;
		default:
			texture = "/net/minecraft/src/buildcraft/energy/gui/trunk_red.png";
			break;
		}

		GL11.glBindTexture(3553 /* GL_TEXTURE_2D */, ModLoader
				.getMinecraftInstance().renderEngine
				.getTexture(texture));
		
		trunk.render(factor);
		
		GL11.glPopMatrix();
	}
}
