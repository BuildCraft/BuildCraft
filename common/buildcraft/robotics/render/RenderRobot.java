/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.render;

import java.util.Date;

import com.mojang.authlib.GameProfile;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.util.Constants.NBT;

import buildcraft.BuildCraftRobotics;
import buildcraft.api.robots.IRobotOverlayItem;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityLaser;
import buildcraft.core.lib.client.render.RenderUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.render.RenderLaser;
import buildcraft.robotics.EntityRobot;

/** All of this is getting a mega-rewrite for Neptune */
public class RenderRobot extends Render<EntityRobot> {
    private static final ResourceLocation overlay_red = new ResourceLocation(DefaultProps.TEXTURE_PATH_ROBOTS + "/overlay_side.png");
    private static final ResourceLocation overlay_cyan = new ResourceLocation(DefaultProps.TEXTURE_PATH_ROBOTS + "/overlay_bottom.png");

    private final EntityItem dummyEntityItem = new EntityItem(null);
    private final RenderEntityItem customRenderItem;
    private final LayerBipedArmor armorRenderer;

    private ModelBase model = new ModelBase() {};
    private ModelBase modelHelmet = new ModelBase() {};
    private ModelBase modelSkullOverlay = new ModelBase() {};
    private ModelRenderer skullOverlayBox;
    private ModelRenderer box, helmetBox;

    public RenderRobot(RenderManager manager) {
        super(manager);
        customRenderItem = new RenderEntityItem(manager, Minecraft.getMinecraft().getRenderItem()) {
            @Override
            public boolean shouldBob() {
                return false;
            }

            @Override
            public boolean shouldSpreadItems() {
                return false;
            }
        };

        armorRenderer = new LayerBipedArmor(new RenderPlayer(manager));

        box = new ModelRenderer(model, 0, 0);
        box.setTextureSize(32, 32);
        box.addBox(-4F, -4F, -4F, 8, 8, 8);
        box.setRotationPoint(0.0F, 0.0F, 0.0F);
        helmetBox = new ModelRenderer(modelHelmet, 0, 0);
        helmetBox.addBox(-4F, -8F, -4F, 8, 8, 8);
        helmetBox.setRotationPoint(0.0F, 0.0F, 0.0F);
        skullOverlayBox = new ModelRenderer(modelSkullOverlay, 32, 0);
        skullOverlayBox.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
        skullOverlayBox.setRotationPoint(0.0F, 0.0F, 0.0F);
    }

    @Override
    public void doRender(EntityRobot robot, double x, double y, double z, float f, float f1) {
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        float robotYaw = this.interpolateRotation(robot.prevRenderYawOffset, robot.renderYawOffset, f1);
        GL11.glRotatef(-robotYaw, 0.0f, 1.0f, 0.0f);

        boolean glasses = isWearingGlasses();
        if (glasses) {
            GlStateManager.disableTexture2D();
        } else {
            if (robot.getStackInSlot(0) != null) {
                GL11.glPushMatrix();
                GL11.glTranslatef(-0.125F, 0, -0.125F);
                doRenderItem(robot.getStackInSlot(0));
                GL11.glColor3f(1, 1, 1);
                GL11.glPopMatrix();
            }

            if (robot.getStackInSlot(1) != null) {
                GL11.glPushMatrix();
                GL11.glTranslatef(+0.125F, 0, -0.125F);
                doRenderItem(robot.getStackInSlot(1));
                GL11.glColor3f(1, 1, 1);
                GL11.glPopMatrix();
            }

            if (robot.getStackInSlot(2) != null) {
                GL11.glPushMatrix();
                GL11.glTranslatef(+0.125F, 0, +0.125F);
                doRenderItem(robot.getStackInSlot(2));
                GL11.glColor3f(1, 1, 1);
                GL11.glPopMatrix();
            }

            if (robot.getStackInSlot(3) != null) {
                GL11.glPushMatrix();
                GL11.glTranslatef(-0.125F, 0, +0.125F);
                doRenderItem(robot.getStackInSlot(3));
                GL11.glColor3f(1, 1, 1);
                GL11.glPopMatrix();
            }

            if (robot.itemInUse != null) {
                GL11.glPushMatrix();

                GL11.glRotatef(robot.itemAimPitch, 0, 0, 1);

                if (robot.itemActive) {
                    long newDate = new Date().getTime();
                    robot.itemActiveStage = (robot.itemActiveStage + (newDate - robot.lastUpdateTime) / 10) % 45;
                    GL11.glRotatef(robot.itemActiveStage, 0, 0, 1);
                    robot.lastUpdateTime = newDate;
                }

                GL11.glTranslatef(-0.4F, 0, 0);
                GL11.glRotatef(-45F + 180F, 0, 1, 0);
                GL11.glScalef(0.8F, 0.8F, 0.8F);

                ItemStack itemstack1 = robot.itemInUse;

                // if (itemstack1.getItem().requiresMultipleRenderPasses()) {
                // for (int k = 0; k < itemstack1.getItem().getRenderPasses(itemstack1.getItemDamage()); ++k) {
                // RenderUtils.setGLColorFromInt(itemstack1.getItem().getColorFromItemStack(itemstack1, k));
                // this.renderManager.itemRenderer.renderItem(robot, itemstack1, k);
                // }
                // } else {
                RenderUtils.setGLColorFromInt(itemstack1.getItem().getColorFromItemStack(itemstack1, 0));
                // this.renderManager.itemRenderer.renderItem(robot, itemstack1, 0);
                Minecraft.getMinecraft().getItemRenderer().renderItem(robot, itemstack1, TransformType.THIRD_PERSON);
                // }

                GL11.glColor3f(1, 1, 1);
                GL11.glPopMatrix();
            }
        }
        if (robot.laser.isVisible) {
            robot.laser.head = Utils.getVec(robot);

            RenderLaser.doRenderLaser(robot.worldObj, renderManager.renderEngine, robot.laser, EntityLaser.LASER_YELLOW);
        }

        if (robot.getTexture() != null) {
            renderManager.renderEngine.bindTexture(robot.getTexture());
            float storagePercent = (float) robot.getBattery().getEnergyStored() / (float) robot.getBattery().getMaxEnergyStored();
            if (robot.hurtTime > 0) {
                GL11.glColor3f(1.0f, 0.6f, 0.6f);
                GL11.glRotatef(robot.hurtTime * 0.01f, 0, 0, 1);
            }
            doRenderRobot(1F / 16F, renderManager.renderEngine, storagePercent, robot.isActive());
        }

        if (glasses) {
            GlStateManager.enableTexture2D();
        } else {
            for (ItemStack s : robot.getWearables()) {
                doRenderWearable(robot, renderManager.renderEngine, s);
            }
        }

        GL11.glPopMatrix();
    }

    private static boolean isWearingGlasses() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        ItemStack helmet = player.getCurrentArmor(3);
        if (helmet == null || helmet.getItem() != BuildCraftRobotics.robotGoggles) {
            return false;
        }
        return true;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityRobot entity) {
        return entity.getTexture();
    }

    // @Override
    // public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
    // if (RenderManager.instance == null || RenderManager.instance.renderEngine == null) {
    // return;
    // }
    //
    // GL11.glPushMatrix();
    //
    // if (item.getItem() == BuildCraftRobotics.robotItem) {
    // ItemRobot robot = (ItemRobot) item.getItem();
    // RenderManager.instance.renderEngine.bindTexture(robot.getTextureRobot(item));
    // }
    //
    // if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
    // GL11.glTranslated(0.0, 1.0, 0.7);
    // } else if (type == ItemRenderType.ENTITY) {
    // GL11.glScaled(0.6, 0.6, 0.6);
    // } else if (type == ItemRenderType.INVENTORY) {
    // GL11.glScaled(1.5, 1.5, 1.5);
    // }
    //
    // doRenderRobot(1F / 16F, RenderManager.instance.renderEngine, 0.9F, false);
    //
    // GL11.glPopMatrix();
    // }

    private void doRenderItem(ItemStack stack) {
        float renderScale = 0.5f;
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0.28F, 0);
        GL11.glScalef(renderScale, renderScale, renderScale);
        dummyEntityItem.setEntityItemStack(stack);
        customRenderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);

        GL11.glPopMatrix();
    }

    private void doRenderWearable(EntityRobot entity, TextureManager textureManager, ItemStack wearable) {
        if (wearable.getItem() instanceof IRobotOverlayItem) {
            ((IRobotOverlayItem) wearable.getItem()).renderRobotOverlay(wearable, textureManager);
        } else if (wearable.getItem() instanceof ItemArmor) {
            GL11.glPushMatrix();
            GL11.glScalef(1.0125F, 1.0125F, 1.0125F);
            GL11.glTranslatef(0.0f, -0.25f, 0.0f);
            GL11.glRotatef(180F, 0, 0, 1);

            int color = wearable.getItem().getColorFromItemStack(wearable, 0);
            if (color != 16777215) {
                GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT);
                GL11.glColor3ub((byte) (color >> 16), (byte) ((color >> 8) & 255), (byte) (color & 255));
            }

            textureManager.bindTexture(armorRenderer.getArmorResource(entity, wearable, 0, null));
            ModelBiped armorModel = ForgeHooksClient.getArmorModel(entity, wearable, 0, null);
            if (armorModel != null) {
                armorModel.render(entity, 0, 0, 0, -90f, 0, 1 / 16F);

                if (color != 16777215) {
                    GL11.glPopAttrib();
                }
            } else {
                GL11.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
                helmetBox.render(1 / 16F);

                if (color != 16777215) {
                    this.bindTexture(new ResourceLocation(ForgeHooksClient.getArmorTexture(entity, wearable, "", 0, "overlay")));
                    helmetBox.render(1 / 16F);
                    GL11.glPopAttrib();
                }
            }

            GL11.glPopMatrix();
        } else if (wearable.getItem() instanceof ItemSkull) {
            doRenderSkull(wearable);
        }
    }

    private void doRenderSkull(ItemStack wearable) {
        GL11.glPushMatrix();
        GL11.glScalef(1.0125F, 1.0125F, 1.0125F);
        GameProfile gameProfile = null;
        if (wearable.hasTagCompound()) {
            NBTTagCompound nbt = wearable.getTagCompound();
            if (nbt.hasKey("Name")) {// FIXME: Come back to this!
                // gameProfile = gameProfileCache.get(nbt.getString("Name"));
            } else if (nbt.hasKey("SkullOwner", NBT.TAG_COMPOUND)) {
                gameProfile = NBTUtil.readGameProfileFromNBT(nbt.getCompoundTag("SkullOwner"));
                nbt.setString("Name", gameProfile.getName());
                // gameProfileCache.put(gameProfile.getName(), gameProfile);
            }
        }

        TileEntitySkullRenderer.instance.renderSkull(-0.5F, -0.25F, -0.5F, EnumFacing.values()[wearable.getItemDamage() & 7], -90.0F, 1, gameProfile,
                0);
        if (gameProfile != null) {
            GL11.glTranslatef(0.0f, -0.25f, 0.0f);
            GL11.glRotatef(180F, 0, 0, 1);
            GL11.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
            skullOverlayBox.render(1 / 16f);
        }
        GL11.glPopMatrix();
    }

    private void doRenderRobot(float factor, TextureManager texManager, float storagePercent, boolean isAsleep) {
        boolean glasses = isWearingGlasses();
        if (glasses) {
            GlStateManager.color(1 - storagePercent, storagePercent, 0);
            /* We always write out very tiny (close) values to the depth buffer. This ensures that we always are above
             * everything else, but we still use depth for rendering ourselves. */
            GL11.glDepthRange(0, 0.001);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        }
        box.render(factor);
        if (glasses) {
            GlStateManager.color(1, 1, 1);
            GL11.glDepthRange(0, 1);
        }

        if (!isAsleep && !glasses) {
            float lastBrightnessX = OpenGlHelper.lastBrightnessX;
            float lastBrightnessY = OpenGlHelper.lastBrightnessY;

            GL11.glPushMatrix();
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

            GL11.glColor4f(1.0F, 1.0F, 1.0F, storagePercent);
            texManager.bindTexture(overlay_red);
            box.render(factor);

            GL11.glDisable(GL11.GL_BLEND);

            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            texManager.bindTexture(overlay_cyan);
            box.render(factor);

            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();

            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
        }

    }

    private float interpolateRotation(float prevRot, float rot, float partialTicks) {
        float angle;

        for (angle = rot - prevRot; angle < -180.0F; angle += 360.0F) {}

        while (angle >= 180.0F) {
            angle -= 360.0F;
        }

        return prevRot + partialTicks * angle;
    }
}
