/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.transport.render;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import buildcraft.core.utils.Utils;

public class TileEntityPickupFX extends EntityFX {

	private double yDestination;

	public TileEntityPickupFX(World world, EntityItem entity, TileEntity entity1) {
		super(world, entity.posX, entity.posY, entity.posZ, entity.motionX, entity.motionY, entity.motionZ);
		field_678_p = 0;
		field_677_q = 0;
		field_675_a = entity;
		field_679_o = entity1;
		field_677_q = 3;

		yDestination = Utils.getPipeFloorOf(entity.func_92014_d());
	}

	@Override
	public void renderParticle(Tessellator tessellator, float f, float f1, float f2, float f3, float f4, float f5) {
		float f6 = (field_678_p + f) / field_677_q;
		f6 *= f6;
		double d = field_675_a.posX;
		double d1 = field_675_a.posY;
		double d2 = field_675_a.posZ;
		double d3 = field_679_o.xCoord + 0.5;
		double d4 = field_679_o.yCoord + yDestination;
		double d5 = field_679_o.zCoord + 0.5;
		double d6 = d + (d3 - d) * f6;
		double d7 = d1 + (d4 - d1) * f6;
		double d8 = d2 + (d5 - d2) * f6;
		int i = MathHelper.floor_double(d6);
		int j = MathHelper.floor_double(d7 + (yOffset / 2.0F));
		int k = MathHelper.floor_double(d8);
		float f7 = worldObj.getLightBrightness(i, j, k);
		d6 -= interpPosX;
		d7 -= interpPosY;
		d8 -= interpPosZ;
		GL11.glColor4f(f7, f7, f7, 1.0F);

		if (RenderManager.instance.renderEngine != null) {
			RenderManager.instance.renderEntityWithPosYaw(field_675_a, (float) d6, (float) d7, (float) d8, field_675_a.rotationYaw, f);
		}
	}

	@Override
	public void onUpdate() {
		field_678_p++;
		if (field_678_p == field_677_q) {
			setDead();
		}
	}

	@Override
	public int getFXLayer() {
		return 3;
	}

	private Entity field_675_a;
	private TileEntity field_679_o;
	private int field_678_p;
	private int field_677_q;
}
