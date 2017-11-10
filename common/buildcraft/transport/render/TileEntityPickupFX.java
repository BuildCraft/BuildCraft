/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 * <p/>
 * Based on EntityPickupFX
 */
/**
 * Based on EntityPickupFX
 */
package buildcraft.transport.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.transport.utils.TransportUtils;

@SideOnly(Side.CLIENT)
public class TileEntityPickupFX extends EntityFX {
	private Entity entityToPickUp;
	private TileEntity entityPickingUp;
	private int age = 0;
	private int maxAge = 0;

	/** renamed from yOffset to fix shadowing Entity.yOffset */
	private double yOffs;

	public TileEntityPickupFX(World par1World, EntityItem par2Entity,
							  TileEntity par3Entity) {
		super(par1World, par2Entity.posX, par2Entity.posY, par2Entity.posZ, par2Entity.motionX, par2Entity.motionY, par2Entity.motionZ);
		this.entityToPickUp = par2Entity;
		this.entityPickingUp = par3Entity;
		this.maxAge = 3;
		this.yOffs = TransportUtils.getPipeFloorOf(par2Entity.getEntityItem());
	}

	@Override
	public void renderParticle(Tessellator par1Tessellator, float par2,
							   float par3, float par4, float par5, float par6, float par7) {
		float var8 = (this.age + par2) / this.maxAge;
		var8 *= var8;
		double var9 = this.entityToPickUp.posX;
		double var11 = this.entityToPickUp.posY;
		double var13 = this.entityToPickUp.posZ;
		double var15 = this.entityPickingUp.xCoord + 0.5;
		double var17 = this.entityPickingUp.yCoord + yOffs;
		double var19 = this.entityPickingUp.zCoord + 0.5;
		double var21 = var9 + (var15 - var9) * var8;
		double var23 = var11 + (var17 - var11) * var8;
		double var25 = var13 + (var19 - var13) * var8;
		int var30 = this.getBrightnessForRender(par2);
		int var31 = var30 % 65536;
		int var32 = var30 / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,
				var31 / 1.0F, var32 / 1.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		var21 -= interpPosX;
		var23 -= interpPosY;
		var25 -= interpPosZ;
		if (RenderManager.instance.renderEngine != null) {
			RenderManager.instance.renderEntityWithPosYaw(this.entityToPickUp,
					var21, var23, var25, this.entityToPickUp.rotationYaw, par2);
		}
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		++this.age;

		if (this.age == this.maxAge) {
			this.setDead();
		}
	}

	@Override
	public int getFXLayer() {
		return 3;
	}
}
