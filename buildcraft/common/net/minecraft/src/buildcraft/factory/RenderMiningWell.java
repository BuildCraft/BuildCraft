package net.minecraft.src.buildcraft.factory;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Entity;
import net.minecraft.src.ModelRenderer;
import net.minecraft.src.RenderBoat;

public class RenderMiningWell extends RenderBoat {

	public ModelRenderer keyboard;
	public ModelRenderer lcd;
	public ModelRenderer leg1;
	public ModelRenderer leg2;
	public ModelRenderer leg3;
	public ModelRenderer leg4;
	public ModelRenderer miningWell;
	
	public RenderMiningWell () {		
		//constructor:
		keyboard = new ModelRenderer(20, 10);
		keyboard.addBox(0F, 0F, 0F, 12, 6, 2);
		keyboard.setPosition(0F, 3F, 0F);
		keyboard.rotateAngleX = 5.427973973702365F;

		lcd = new ModelRenderer(-1, 51);
		lcd.addBox(10F, 9F, -1F, 14, 10, 3);
		lcd.setPosition(0F, 0F, 9F);
		lcd.rotateAngleY = 5.811946409141117F;

		leg1 = new ModelRenderer(0, 0);
		leg1.addBox(0F, 0F, 0F, 5, 15, 5);
		leg1.setPosition(1F, 0F, 7F);

		leg2 = new ModelRenderer(0, 0);
		leg2.addBox(0F, 0F, 0F, 5, 15, 5);
		leg2.setPosition(10F, 0F, 7F);

		leg3 = new ModelRenderer(0, 0);
		leg3.addBox(0F, 0F, 0F, 5, 15, 5);
		leg3.setPosition(10F, 0F, 16F);

		leg4 = new ModelRenderer(0, 0);
		leg4.addBox(0F, 0F, 0F, 5, 15, 5);
		leg4.setPosition(1F, 0F, 16F);

		miningWell = new ModelRenderer(0, 20);
		miningWell.addBox(0F, 0F, 0F, 16, 16, 16);
		miningWell.setPosition(0F, 5F, 6F);

	}

	@Override
	public void doRender(Entity entity, double x, double y, double z,
			float angle, float f1) {
        
		GL11.glPushMatrix();
		GL11.glTranslatef((float)x,(float)y, (float)z);
		GL11.glRotatef(angle, 0.0F, 1.0F, 0.0F);
		
		if (renderManager != null && renderManager.renderEngine != null) {
			loadTexture("/net/minecraft/src/buildcraft/factory/gui/mining_well.png");
		}
        
		float reduction = (float) (1.0 / 16.0);
		
		//render:
		keyboard.render(reduction);
		lcd.render(reduction);
		leg1.render(reduction);
		leg2.render(reduction);
		leg3.render(reduction);
		leg4.render(reduction);
		miningWell.render(reduction);

		GL11.glPopMatrix();
		
	}
	
}