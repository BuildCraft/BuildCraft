/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
/** Based on EntityPickupFX */
package buildcraft.transport.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.transport.utils.TransportUtils;

@SideOnly(Side.CLIENT)
public class TileEntityPickupFX extends EntityFX {
    private Entity entityToPickUp;
    private TileEntity tilePickingUp;
    private int age = 0;
    private int maxAge = 0;

    /** renamed from yOffset to fix shadowing Entity.yOffset */
    private double yOffs;

    public TileEntityPickupFX(World par1World, EntityItem par2Entity, TileEntity par3Entity) {
        super(par1World, par2Entity.posX, par2Entity.posY, par2Entity.posZ, par2Entity.motionX, par2Entity.motionY, par2Entity.motionZ);
        this.entityToPickUp = par2Entity;
        this.tilePickingUp = par3Entity;
        this.maxAge = 3;
        this.yOffs = TransportUtils.getPipeFloorOf(par2Entity.getEntityItem());
    }

    @Override
    public void renderParticle(WorldRenderer wr, Entity ent, float partialTicks, float par3, float par4, float par5, float par6, float par7) {
        float agePercent = (this.age + partialTicks) / this.maxAge;
        agePercent *= agePercent;
        double entX = this.entityToPickUp.posX;
        double entY = this.entityToPickUp.posY;
        double entZ = this.entityToPickUp.posZ;
        double tileX = this.tilePickingUp.getPos().getX() + 0.5;
        double tileY = this.tilePickingUp.getPos().getY() + yOffs;
        double tileZ = this.tilePickingUp.getPos().getZ() + 0.5;
        double var21 = entX + (tileX - entX) * agePercent;
        double var23 = entY + (tileY - entY) * agePercent;
        double var25 = entZ + (tileZ - entZ) * agePercent;
        int brightness = this.getBrightnessForRender(partialTicks);
        int var31 = brightness % 0x10000;
        int var32 = brightness / 0x10000;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var31 / 1.0F, var32 / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        var21 -= interpPosX;
        var23 -= interpPosY;
        var25 -= interpPosZ;
        if (Minecraft.getMinecraft().renderEngine != null) {
            Minecraft.getMinecraft().getRenderManager().renderEntityWithPosYaw(this.entityToPickUp, var21, var23, var25,
                    this.entityToPickUp.rotationYaw, partialTicks);
        }
    }

    /** Called to update the entity's position/logic. */
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
