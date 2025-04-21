/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.robotics.client.render;

import buildcraft.api.robots.IRobotOverlayItem;
import buildcraft.core.client.BuildCraftLaserManager;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.render.laser.LaserRenderer_BC8;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.robotics.BCRoboticsItems;
import buildcraft.robotics.entity.EntityRobot;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.AbstractSkullBlock;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.SkullTileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeableArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/** All of this is getting a mega-rewrite for Neptune */
// public class RenderRobot extends Render<EntityRobot>
public class RenderRobot extends EntityRenderer<EntityRobot> {
    // private static final ResourceLocation overlay_red = new ResourceLocation(DefaultProps.TEXTURE_PATH_ROBOTS + "/overlay_side.png");
    private static final ResourceLocation overlay_red = new ResourceLocation("buildcraftrobotics:textures/entities" + "/overlay_side.png");
    // private static final ResourceLocation overlay_cyan = new ResourceLocation(DefaultProps.TEXTURE_PATH_ROBOTS + "/overlay_bottom.png");
    private static final ResourceLocation overlay_cyan = new ResourceLocation("buildcraftrobotics:textures/entities" + "/overlay_bottom.png");

    // private final ItemEntity dummyEntityItem = new ItemEntity(null);
    // private final RenderEntityItem customRenderItem;

    // private ModelBase model = new ModelBase() {};
    // private ModelBase modelHelmet = new ModelBase() {};
    // private ModelBase modelSkullOverlay = new ModelBase() {};
    private ModelRenderer skullOverlayBox;
    private ModelRenderer box, helmetBox;
    private static final Function<ResourceLocation, RenderType> ENTITY_CUTOUT_NO_CULL_NO_DEPTH = (textureLocation) -> {
        RenderType.State rendertype$compositestate = RenderType.State.builder().setTextureState(new RenderState.TextureState(textureLocation, false, false)).setDepthTestState(RenderState.NO_DEPTH_TEST).setTransparencyState(RenderState.NO_TRANSPARENCY).setCullState(RenderState.NO_CULL).setLightmapState(RenderState.LIGHTMAP).setOverlayState(RenderState.OVERLAY).createCompositeState(true);
        return RenderType.create("entity_cutout_no_cull", DefaultVertexFormats.NEW_ENTITY, GL11.GL_QUADS, 256, true, false, rendertype$compositestate);
    };

    public RenderRobot(EntityRendererManager context) {
        super(context);
        // customRenderItem = new RenderEntityItem(Minecraft.getMinecraft().getRenderManager(), Minecraft.getMinecraft().getRenderItem())
        // customRenderItem = new ItemEntityRenderer(context) {
        //      @Override
        //     public boolean shouldBob() {
        //         return false;
        //     }

        //    @Override
        //    public boolean shouldSpreadItems() {
        //        return false;
        //    }
        // };

//        box = new ModelRenderer(model, 0, 0);
//        box.addBox(-4F, -4F, -4F, 8, 8, 8);
//        box.setRotationPoint(0.0F, 0.0F, 0.0F);
        box = new ModelRenderer(32, 32, 0, 0);
        box.addBox(-4F, -4F, -4F, 8, 8, 8, 0.0F);

//        helmetBox = new ModelRenderer(modelHelmet, 0, 0);
//        helmetBox.addBox(-4F, -8F, -4F, 8, 8, 8);
//        helmetBox.setRotationPoint(0.0F, 0.0F, 0.0F);
        helmetBox = new ModelRenderer(64, 32, 0, 0);
        helmetBox.addBox(-4F, -8F, -4F, 8, 8, 8, 1.0F);

//        skullOverlayBox = new ModelRenderer(modelSkullOverlay, 32, 0);
//        skullOverlayBox.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.5F);
//        skullOverlayBox.setRotationPoint(0.0F, 0.0F, 0.0F);
        skullOverlayBox = new ModelRenderer(64, 32, 0, 0);
        skullOverlayBox.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0, 0.5F, 0.5F);
    }

    @Override
    // public void doRender(EntityRobot entity, double x, double y, double z, float f, float f1)
    public void render(EntityRobot robot, float entityYaw, float partialTicks, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int packedLight) {
        packedLight = RenderUtil.getCombinedLight(robot.level, robot.blockPosition());

        // GL11.glPushMatrix();
        poseStack.pushPose();
        // GL11.glTranslated(x, y, z);

        // float robotYaw = this.interpolateRotation(robot.prevRenderYawOffset, robot.renderYawOffset, f1);
        float robotYaw = this.interpolateRotation(robot.yBodyRotO, robot.yBodyRot, partialTicks);
        // GL11.glRotatef(-robotYaw, 0.0f, 1.0f, 0.0f);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-robotYaw));

        boolean glasses = isWearingGlasses();
        if (glasses) {
            // GlStateManager.disableTexture2D();
        } else {
            IItemHandler itemHandler = robot.getCapability(CapUtil.CAP_ITEMS).orElse(null);
            // if (!robot.getStackInSlot(0).isEmpty())
            if (!itemHandler.getStackInSlot(0).isEmpty()) {
                // GL11.glPushMatrix();
                poseStack.pushPose();
                // GL11.glTranslatef(-0.125F, 0, -0.125F);
                poseStack.translate(-0.125F, 0, -0.125F);
                // doRenderItem(robot.getStackInSlot(0));
                doRenderItem(itemHandler.getStackInSlot(0), poseStack, bufferSource, packedLight, robot);
                // GL11.glColor3f(1, 1, 1);
                RenderUtil.color(1, 1, 1);
                // GL11.glPopMatrix();
                poseStack.popPose();
            }

            // if (!robot.getStackInSlot(1).isEmpty())
            if (!itemHandler.getStackInSlot(1).isEmpty()) {
                // GL11.glPushMatrix();
                poseStack.pushPose();
                // GL11.glTranslatef(+0.125F, 0, -0.125F);
                poseStack.translate(+0.125F, 0, -0.125F);
                // doRenderItem(robot.getStackInSlot(1));
                doRenderItem(itemHandler.getStackInSlot(1), poseStack, bufferSource, packedLight, robot);
                // GL11.glColor3f(1, 1, 1);
                RenderUtil.color(1, 1, 1);
                // GL11.glPopMatrix();
                poseStack.popPose();
            }

            // if (!robot.getStackInSlot(2).isEmpty())
            if (!itemHandler.getStackInSlot(2).isEmpty()) {
                // GL11.glPushMatrix();
                poseStack.pushPose();
                // GL11.glTranslatef(+0.125F, 0, +0.125F);
                poseStack.translate(+0.125F, 0, +0.125F);
                // doRenderItem(robot.getStackInSlot(2));
                doRenderItem(itemHandler.getStackInSlot(2), poseStack, bufferSource, packedLight, robot);
                // GL11.glColor3f(1, 1, 1);
                RenderUtil.color(1, 1, 1);
                // GL11.glPopMatrix();
                poseStack.popPose();
            }

            // if (!robot.getStackInSlot(3).isEmpty())
            if (!itemHandler.getStackInSlot(3).isEmpty()) {
                // GL11.glPushMatrix();
                poseStack.pushPose();
                // GL11.glTranslatef(-0.125F, 0, +0.125F);
                poseStack.translate(-0.125F, 0, +0.125F);
                // doRenderItem(robot.getStackInSlot(3));
                doRenderItem(itemHandler.getStackInSlot(3), poseStack, bufferSource, packedLight, robot);
                // GL11.glColor3f(1, 1, 1);
                RenderUtil.color(1, 1, 1);
                // GL11.glPopMatrix();
                poseStack.popPose();
            }

            // if (robot.itemInUse != null)
            if (!robot.itemInUse.isEmpty()) {
                // GL11.glPushMatrix();
                poseStack.pushPose();

                // GL11.glRotatef(robot.itemAimPitch, 0, 0, 1);
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(robot.itemAimPitch));

                if (robot.itemActive) {
                    long newDate = new Date().getTime();
                    robot.itemActiveStage = (robot.itemActiveStage + (newDate - robot.lastUpdateTime) / 10) % 45;
                    // GL11.glRotatef(robot.itemActiveStage, 0, 0, 1);
                    poseStack.mulPose(Vector3f.ZP.rotationDegrees(robot.itemActiveStage));
                    robot.lastUpdateTime = newDate;
                }

                // GL11.glTranslatef(-0.4F, 0, 0);
                poseStack.translate(-0.4F, 0, 0);
                // GL11.glRotatef(-45F + 180F, 0, 1, 0);
                poseStack.mulPose(Vector3f.YP.rotationDegrees(-45F + 180F));
                // GL11.glScalef(0.8F, 0.8F, 0.8F);
                poseStack.scale(0.8F, 0.8F, 0.8F);

                ItemStack itemstack1 = robot.itemInUse;

                // if (itemstack1.getItem().requiresMultipleRenderPasses()) {
                // for (int k = 0; k < itemstack1.getItem().getRenderPasses(itemstack1.getItemDamage()); ++k) {
                // RenderUtils.setGLColorFromInt(itemstack1.getItem().getColorFromItemStack(itemstack1, k));
                // this.renderManager.itemRenderer.renderItem(robot, itemstack1, k);
                // }
                // } else {
                // RenderUtils.setGLColorFromInt(itemstack1.getItem().getColorFromItemStack(itemstack1, 0));
                // this.renderManager.itemRenderer.renderItem(robot, itemstack1, 0);
                // Minecraft.getMinecraft().getItemRenderer().renderItem(robot, itemstack1, TransformType.THIRD_PERSON);
                Minecraft.getInstance().getItemRenderer().renderStatic(robot, itemstack1, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, false, poseStack, bufferSource, robot.level, packedLight, OverlayTexture.NO_OVERLAY);
                // }

                // GL11.glColor3f(1, 1, 1);
                RenderUtil.color(1, 1, 1);
                // GL11.glPopMatrix();
                poseStack.popPose();
            }
        }
        // if (robot.laser.isVisible)
        if (robot.isLaserVisible) {
            // robot.laser.start = VecUtil.getVec(robot);
            LaserData_BC8 laser = new LaserData_BC8(BuildCraftLaserManager.POWER_MED, VecUtil.getVec(robot), new Vector3d(robot.laserEndX, robot.laserEndY, robot.laserEndZ), 0.5 / 16);

            // RenderLaser.doRenderLaser(robot.level, renderManager.renderEngine, robot.laser, EntityLaser.LASER_YELLOW);
            LaserRenderer_BC8.renderLaserDynamic(laser, poseStack.last(), bufferSource.getBuffer(RenderType.cutout()));
        }

        if (robot.getTexture() != null) {
            // renderManager.renderEngine.bindTexture(robot.getTexture());
            float storagePercent = (float) robot.getBattery().getStored() / (float) robot.getBattery().getCapacity();
            if (robot.hurtTime > 0) {
                // GL11.glColor3f(1.0f, 0.6f, 0.6f);
                RenderUtil.color(1.0f, 0.6f, 0.6f);
                // GL11.glRotatef(robot.hurtTime * 0.01f, 0, 0, 1);
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(robot.hurtTime * 0.01f));
            }
            // doRenderRobot(1F / 16F, renderManager.renderEngine, storagePercent, robot.isActive());
            doRenderRobot(1, robot.getTexture(), storagePercent, robot.isActive(), poseStack, bufferSource, packedLight);
        }

        if (glasses) {
            // GlStateManager.enableTexture2D();
        } else {
            for (ItemStack s : robot.getWearables()) {
                // doRenderWearable(robot, renderManager.renderEngine, s);
                doRenderWearable(robot, s, poseStack, bufferSource, packedLight);
            }
        }

        // GL11.glPopMatrix();
        poseStack.popPose();
    }

    private boolean isWearingGlasses() {
        PlayerEntity player = Minecraft.getInstance().player;
        // ItemStack helmet = player.getCurrentArmor(3);
        ItemStack helmet = player.getItemBySlot(EquipmentSlotType.HEAD);
        // if (helmet == null || helmet.getItem() != BCRoboticsItems.robotGoggles.get())
        if (helmet.isEmpty() || helmet.getItem() != BCRoboticsItems.robotGoggles.get()) {
            return false;
        }
        return true;
    }

    @Override
    // protected ResourceLocation getEntityTexture(EntityRobot entity)
    public ResourceLocation getTextureLocation(EntityRobot entity) {
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
    // if (item.getItem() == BCRobotics.robotItem) {
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

    // private void doRenderItem(ItemStack stack)
    private void doRenderItem(ItemStack stack, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int packedLight, EntityRobot robot) {
        float renderScale = 0.5f;
        // GL11.glPushMatrix();
        poseStack.pushPose();
        // GL11.glTranslatef(0, 0.28F, 0);
        poseStack.translate(0, 0.28F, 0);
        // GL11.glScalef(renderScale, renderScale, renderScale);
        poseStack.scale(renderScale, renderScale, renderScale);
        // dummyEntityItem.setEntityItemStack(stack);
        // customRenderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);
        Minecraft.getInstance().getItemRenderer().renderStatic(robot, stack, ItemCameraTransforms.TransformType.GROUND, false, poseStack, bufferSource, robot.level, packedLight, OverlayTexture.NO_OVERLAY);

        // GL11.glPopMatrix();
        poseStack.popPose();
    }

    // private void doRenderWearable(EntityRobot entity, TextureManager textureManager, ItemStack wearable)
    private void doRenderWearable(EntityRobot entity, ItemStack wearable, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int packedLight) {
        if (wearable.getItem() instanceof IRobotOverlayItem) {
            // ((IRobotOverlayItem) wearable.getItem()).renderRobotOverlay(wearable, textureManager);
            ((IRobotOverlayItem) wearable.getItem()).renderRobotOverlay(wearable);
        } else if (wearable.getItem() instanceof ArmorItem) {
            // GL11.glPushMatrix();
            poseStack.pushPose();
            // GL11.glScalef(1.0125F, 1.0125F, 1.0125F);
            poseStack.scale(1.0125F, 1.0125F, 1.0125F);
            // GL11.glTranslatef(0.0f, -0.25f, 0.0f);
            poseStack.translate(0.0f, -0.25f, 0.0f);
            // GL11.glRotatef(180F, 0, 0, 1);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(180F));

            // int color = wearable.getItem().getColorFromItemStack(wearable, 0);
            int color;
            if (wearable.getItem() instanceof DyeableArmorItem) {
                color = ((DyeableArmorItem) wearable.getItem()).getColor(wearable);
            } else {
                color = 16777215;
            }
            // if (color != 16777215) {
            //     GL11.glPushAttrib(GL11.GL_COLOR_BUFFER_BIT);
            //     GL11.glColor3ub((byte) (color >> 16), (byte) ((color >> 8) & 255), (byte) (color & 255));
            // }

            // textureManager.bindTexture(new ResourceLocation(ForgeHooksClient.getArmorTexture(entity, wearable, "", 0, "")));
            ResourceLocation armorTexture = new ResourceLocation(ForgeHooksClient.getArmorTexture(entity, wearable, "", EquipmentSlotType.HEAD, ""));
            // ModelBiped armorModel = ForgeHooksClient.getArmorModel(entity, wearable, 0, null);
            BipedModel armorModel = ForgeHooksClient.getArmorModel(entity, wearable, EquipmentSlotType.HEAD, null);
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0f));
            // poseStack.scale(1 / 16F, 1 / 16F, 1 / 16F);
            boolean foil = wearable.hasFoil();
            if (armorModel != null) {
                // armorModel.render(entity, 0, 0, 0, -90f, 0, 1 / 16F);

                // if (color != 16777215) {
                //     GL11.glPopAttrib();
                // }

                if (color != 16777215) {
                    float f = (float) (color >> 16 & 255) / 255.0F;
                    float f1 = (float) (color >> 8 & 255) / 255.0F;
                    float f2 = (float) (color & 255) / 255.0F;
                    this.renderModel(poseStack, bufferSource, packedLight, foil, armorModel, f, f1, f2, this.getArmorResource(entity, wearable, EquipmentSlotType.HEAD, null));
                    this.renderModel(poseStack, bufferSource, packedLight, foil, armorModel, 1.0F, 1.0F, 1.0F, this.getArmorResource(entity, wearable, EquipmentSlotType.HEAD, "overlay"));
                } else {
                    this.renderModel(poseStack, bufferSource, packedLight, foil, armorModel, 1.0F, 1.0F, 1.0F, this.getArmorResource(entity, wearable, EquipmentSlotType.HEAD, null));
                }
            } else {
                // GL11.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
                // helmetBox.render(1 / 16F);

                // if (color != 16777215) {
                //     this.bindTexture(new ResourceLocation(ForgeHooksClient.getArmorTexture(entity, wearable, "", 0, "overlay")));
                //     helmetBox.render(1 / 16F);
                //     GL11.glPopAttrib();
                // }

                if (color != 16777215) {
                    // GL11.glPopAttrib();
                    float f = (float) (color >> 16 & 255) / 255.0F;
                    float f1 = (float) (color >> 8 & 255) / 255.0F;
                    float f2 = (float) (color & 255) / 255.0F;
                    this.renderModel(poseStack, bufferSource, packedLight, foil, helmetBox, f, f1, f2, this.getArmorResource(entity, wearable, EquipmentSlotType.HEAD, null));
                    this.renderModel(poseStack, bufferSource, packedLight, foil, helmetBox, 1.0F, 1.0F, 1.0F, this.getArmorResource(entity, wearable, EquipmentSlotType.HEAD, "overlay"));
                } else {
                    this.renderModel(poseStack, bufferSource, packedLight, foil, helmetBox, 1.0F, 1.0F, 1.0F, this.getArmorResource(entity, wearable, EquipmentSlotType.HEAD, null));
                }
            }
            poseStack.popPose();

            // GL11.glPopMatrix();
            poseStack.popPose();
        }
        // else if (wearable.getItem() instanceof ItemSkull)
        else if (wearable.getItem() instanceof BlockItem && ((BlockItem) wearable.getItem()).getBlock() instanceof AbstractSkullBlock) {
            // doRenderSkull(wearable);
            doRenderSkull(wearable, poseStack, bufferSource, packedLight);
        }
    }

    // private void doRenderSkull(ItemStack wearable)
    private void doRenderSkull(ItemStack wearable, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int packedLight) {
        // GL11.glPushMatrix();
        poseStack.pushPose();
        // GL11.glScalef(1.0125F, 1.0125F, 1.0125F);
        poseStack.scale(1.0125F, 1.0125F, 1.0125F);
        GameProfile gameProfile = null;
        if (wearable.hasTag()) {
            CompoundNBT nbt = wearable.getTag();
            if (nbt.contains("Name")) {// FIXME: Come back to this!
                // gameProfile = gameProfileCache.get(nbt.getString("Name"));
            } else if (nbt.contains("SkullOwner", Constants.NBT.TAG_COMPOUND)) {
                gameProfile = NBTUtil.readGameProfile(nbt.getCompound("SkullOwner"));
                // nbt.putString("Name", gameProfile.getName());
                // gameProfileCache.put(gameProfile.getName(), gameProfile);
            }
        }

//        TileEntitySkullRenderer.instance.renderSkull(-0.5F, -0.25F, -0.5F, Direction.values()[wearable.getDamageValue() & 7], -90.0F, 1, gameProfile, 0);
//        if (gameProfile != null) {
//            // GL11.glTranslatef(0.0f, -0.25f, 0.0f);
//            poseStack.translate(0.0f, -0.25f, 0.0f);
//            // GL11.glRotatef(180F, 0, 0, 1);
//            poseStack.mulPose(Vector3f.ZP.rotationDegrees(180F));
//            // GL11.glRotatef(-90.0f, 0.0f, 1.0f, 0.0f);
//            poseStack.mulPose(Vector3f.YP.rotationDegrees(-90.0f));
//            skullOverlayBox.render(1 / 16f);
//        }
        poseStack.translate(-0.5D, -0.25D, -0.5D);
        SkullBlock.ISkullType skullblock$type = ((AbstractSkullBlock) ((BlockItem) wearable.getItem()).getBlock()).getType();
        SkullTileEntityRenderer.renderSkull(null, 180.0F, skullblock$type, gameProfile, 0, poseStack, bufferSource, packedLight);
        // GL11.glPopMatrix();
        poseStack.popPose();
    }

    // private void doRenderRobot(float factor, TextureManager texManager, float storagePercent, boolean isAsleep)
    private void doRenderRobot(float factor, ResourceLocation texture, float storagePercent, boolean isAsleep, MatrixStack poseStack, IRenderTypeBuffer bufferSource, int packedLight) {
        boolean glasses = isWearingGlasses();
        // if (glasses) {
        //     GlStateManager.color(1 - storagePercent, storagePercent, 0);
        //     GlStateManager.disableDepth();
        // }
        // box.render(factor);
        // if (glasses) {
        //     GlStateManager.color(1, 1, 1);
        //     GlStateManager.enableDepth();
        // }
        poseStack.pushPose();
        if (glasses) {
            box.render(poseStack, bufferSource.getBuffer(ENTITY_CUTOUT_NO_CULL_NO_DEPTH.apply(texture)), packedLight, OverlayTexture.NO_OVERLAY, 1 - storagePercent, storagePercent, 0, 1.0F);
        } else {
            box.render(poseStack, bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture)), packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
        poseStack.popPose();

        if (!isAsleep && !glasses) {
            // float lastBrightnessX = OpenGlHelper.lastBrightnessX;
            // float lastBrightnessY = OpenGlHelper.lastBrightnessY;

            // GL11.glPushMatrix();
            poseStack.pushPose();
            // GL11.glEnable(GL11.GL_BLEND);
            // GL11.glEnable(GL11.GL_ALPHA_TEST);
            // GL11.glDisable(GL11.GL_LIGHTING);
            // GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            // OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);

            // GL11.glColor4f(1.0F, 1.0F, 1.0F, storagePercent);
            // texManager.bindTexture(overlay_red);
            // box.render(factor);
            box.render(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(overlay_red)), packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, storagePercent);

            // GL11.glDisable(GL11.GL_BLEND);

            // GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            // texManager.bindTexture(overlay_cyan);
            // box.render(factor);
            box.render(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(overlay_cyan)), packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

            // GL11.glEnable(GL11.GL_LIGHTING);
            // GL11.glPopMatrix();
            poseStack.popPose();

            // OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
        }

    }

    private float interpolateRotation(float prevRot, float rot, float partialTicks) {
        float angle;

        for (angle = rot - prevRot; angle < -180.0F; angle += 360.0F) {
        }

        while (angle >= 180.0F) {
            angle -= 360.0F;
        }

        return prevRot + partialTicks * angle;
    }

    // Calen 1.18.2 from HumanoidArmorLayer
    private void renderModel(MatrixStack poseStack, IRenderTypeBuffer bufferSource, int packedLight, boolean foil, BipedModel model, float r, float g, float b, ResourceLocation armorResource) {
        IVertexBuilder vertexconsumer = ItemRenderer.getArmorFoilBuffer(bufferSource, RenderType.armorCutoutNoCull(armorResource), false, foil);
        model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
    }

    private void renderModel(MatrixStack poseStack, IRenderTypeBuffer bufferSource, int packedLight, boolean foil, ModelRenderer model, float r, float g, float b, ResourceLocation armorResource) {
        IVertexBuilder vertexconsumer = ItemRenderer.getArmorFoilBuffer(bufferSource, RenderType.armorCutoutNoCull(armorResource), false, foil);
        model.render(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
    }

    private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();

    public ResourceLocation getArmorResource(Entity entity, ItemStack stack, EquipmentSlotType slot, @Nullable String type) {
        ArmorItem item = (ArmorItem) stack.getItem();
        String texture = item.getMaterial().getName();
        String domain = "minecraft";
        int idx = texture.indexOf(':');
        if (idx != -1) {
            domain = texture.substring(0, idx);
            texture = texture.substring(idx + 1);
        }
        String s1 = String.format(Locale.ROOT, "%s:textures/models/armor/%s_layer_%d%s.png", domain, texture, 1, type == null ? "" : String.format(Locale.ROOT, "_%s", type));

        s1 = ForgeHooksClient.getArmorTexture(entity, stack, s1, slot, type);
        ResourceLocation resourcelocation = ARMOR_LOCATION_CACHE.get(s1);

        if (resourcelocation == null) {
            resourcelocation = new ResourceLocation(s1);
            ARMOR_LOCATION_CACHE.put(s1, resourcelocation);
        }

        return resourcelocation;
    }
}
