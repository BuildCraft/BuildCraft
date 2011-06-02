package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.ModelBase;
//Exported java file
//Keep in mind that you still need to fill in some blanks
// - ZeuX
import net.minecraft.src.ModelRenderer;

public class QuarryModel extends ModelBase
{		
	public QuarryModel()
	{
		float scale = 0F;
		quary = new ModelRenderer(0, 20);
		quary.addBox(0F, 0F, 0F, 16, 16, 16, scale);
		//quary.setPosition(-8F, 8F, -8F);

		lcd = new ModelRenderer(1, 53);
		lcd.addBox(0F, 0F, 0F, 12, 8, 2, scale);
		//lcd.setPosition(0F, 9F, -9F);
		lcd.rotateAngleY = 0.22689F;

	}
	public void render(float f, float f1, float f2, float f3, float f4, float f5)
	{
		//for animation
		//setRotationAngles(f, f1, f2, f3, f4, f5);
		quary.render(f5);
		lcd.render(f5);
	}

	//fields

	ModelRenderer quary;
	ModelRenderer lcd;

}
