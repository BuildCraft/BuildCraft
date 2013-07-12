/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.render;

import buildcraft.core.EntityEnergyLaser;
import buildcraft.core.EntityLaser;
import net.minecraft.client.model.ModelRenderer;

public class RenderEnergyLaser extends RenderLaser {

	private ModelRenderer box[] = new ModelRenderer[40];

	public RenderEnergyLaser() {
		for (int i = 0; i < box.length; ++i) {
			box[i] = new ModelRenderer(model, box.length - i, 0);
			box[i].addBox(0, -0.5F, -0.5F, 16, 1, 1);
			box[i].rotationPointX = 0;
			box[i].rotationPointY = 0;
			box[i].rotationPointZ = 0;
		}
	}

	@Override
	protected ModelRenderer getBox(EntityLaser laser) {
		EntityEnergyLaser eLaser = (EntityEnergyLaser) laser;

		return box[eLaser.displayStage / 10];
	}

	@Override
	protected void iterate(EntityLaser laser) {
		EntityEnergyLaser eLaser = (EntityEnergyLaser) laser;

		eLaser.displayStage = eLaser.displayStage + 1;
		if (eLaser.displayStage >= box.length * 10) {
			eLaser.displayStage = 0;
		}
	}
}
