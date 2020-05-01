/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.render;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.vecmath.Vector3f;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.EnumPipePart;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.client.model.MutableQuad;
import buildcraft.lib.misc.ItemStackKey;

@SideOnly(Side.CLIENT)
public class ItemRenderUtil {

    private static final LoadingCache<ItemStackKey, Integer> glListCache;

    private static final Random modelOffsetRandom = new Random(0);

    private static final EntityItem dummyEntityItem = new EntityItem(null);
    private static final RenderEntityItem customItemRenderer =
        new RenderEntityItem(Minecraft.getMinecraft().getRenderManager(), Minecraft.getMinecraft().getRenderItem()) {
            @Override
            public boolean shouldSpreadItems() {
                return false;
            }

            @Override
            public boolean shouldBob() {
                return false;
            }
        };
    static {
        glListCache = CacheBuilder.newBuilder()//
            .expireAfterAccess(40, TimeUnit.SECONDS)//
            .removalListener(ItemRenderUtil::onStackRemove)//
            .build(CacheLoader.from(ItemRenderUtil::makeItemGlList));
    }

    private static Integer makeItemGlList(ItemStackKey item) {
        int list = GLAllocation.generateDisplayLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        renderItemImpl(0, 0, 0, item.baseStack);
        GL11.glEndList();
        return list;
    }

    private static void onStackRemove(RemovalNotification<ItemStackKey, Integer> notification) {
        Integer val = notification.getValue();
        if (val != null) {
            GLAllocation.deleteDisplayLists(val);
        }
    }

    private static void renderItemImpl(double x, double y, double z, ItemStack stack) {
        GL11.glPushMatrix();
        GL11.glTranslated(0, -0.2, 0);
        GL11.glScaled(0.9, 0.9, 0.9);

        // This is broken - some stacks render too big but some render way too small.
        // Also not all stacks are centered :/

        if (stack.getItem() instanceof ItemBlock) {
            dummyEntityItem.hoverStart = 0;
        } else {
            // Items are rotated by 45 degrees
            dummyEntityItem.hoverStart = (float) (45 * Math.PI / 180);
        }

        dummyEntityItem.setItem(stack);
        customItemRenderer.doRender(dummyEntityItem, x, y, z, 0, 0);

        GL11.glPopMatrix();
    }

    // Batch item rendering

    private static boolean inBatch = false;

    /** Used to render a lot of items in sequential order. Assumes that you don't change the glstate inbetween calls.
     * You must call {@link #endItemBatch()} after your have rendered all of the items. */
    public static void renderItemStack(double x, double y, double z, ItemStack stack, int lightc, EnumFacing dir,
        BufferBuilder bb) {
        renderItemStack(x, y, z, stack, stack.getCount(), lightc, dir, bb);
    }

    /** Used to render a lot of items in sequential order. Assumes that you don't change the glstate inbetween calls.
     * You must call {@link #endItemBatch()} after your have rendered all of the items. */
    public static void renderItemStack(double x, double y, double z, ItemStack stack, int stackCount, int lightc,
        EnumFacing dir, BufferBuilder bb) {
        if (stack.isEmpty()) {
            return;
        }
        try {
            renderItemStackInternal(x, y, z, stack, stackCount, lightc, dir, bb);
        } catch (Throwable exception) {
            CrashReport report = CrashReport.makeCrashReport(exception, "Rendering Item Stack");
            CrashReportCategory category = report.makeCategory("Item being rendered");
            category.addCrashSection("Stack Count", stackCount);
            category.addDetail("Item Class", () -> "" + stack.getItem().getClass());
            category.addDetail("Item ID", () -> "" + ForgeRegistries.ITEMS.getKey(stack.getItem()));
            category.addDetail("Item Meta", () -> "" + stack.getMetadata());
            category.addDetail("Item NBT", () -> "" + stack.getTagCompound());
            throw new ReportedException(report);
        }
    }

    private static void renderItemStackInternal(
        double x, double y, double z, ItemStack stack, int stackCount, int lightc, EnumFacing dir, BufferBuilder bb
    ) {
        if (dir == null) {
            dir = EnumFacing.EAST;
        }
        dir = BCLibConfig.rotateTravelingItems.changeFacing(dir);

        IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(stack);
        model = model.getOverrides().handleItemState(model, stack, null, null);
        boolean requireGl = stack.hasEffect() || model.isBuiltInRenderer();

        if (bb != null && !requireGl) {

            final int itemModelCount = getStackModelCount(stackCount);

            if (itemModelCount > 1) {
                setupModelOffsetRandom(stack);
            }

            for (int i = 0; i < itemModelCount; i++) {
                if (i == 0) {
                    bb.setTranslation(x, y, z);
                } else {
                    float dx = (modelOffsetRandom.nextFloat() * 2.0F - 1.0F) * 0.08F;
                    float dy = (modelOffsetRandom.nextFloat() * 2.0F - 1.0F) * 0.08F;
                    float dz = (modelOffsetRandom.nextFloat() * 2.0F - 1.0F) * 0.08F;
                    bb.setTranslation(x + dx, y + dy, z + dz);
                }

                float scale = 0.30f;

                MutableQuad q = new MutableQuad(-1, null);
                for (EnumPipePart part : EnumPipePart.VALUES) {
                    for (BakedQuad quad : model.getQuads(null, part.face, 0)) {
                        q.fromBakedItem(quad);
                        q.translated(-0.5, -0.5, -0.5);
                        q.scaled(scale);
                        q.rotate(EnumFacing.SOUTH, dir, 0, 0, 0);
                        if (quad.hasTintIndex()) {
                            int colour =
                                Minecraft.getMinecraft().getItemColors().colorMultiplier(stack, quad.getTintIndex());
                            if (EntityRenderer.anaglyphEnable) {
                                colour = TextureUtil.anaglyphColor(colour);
                            }
                            q.multColouri(colour, colour >> 8, colour >> 16, 0xFF);
                        }
                        q.lighti(lightc);
                        Vector3f normal = q.getCalculatedNormal();
                        q.normalvf(normal);
                        q.multShade();
                        q.render(bb);
                    }
                }
            }

            bb.setTranslation(0, 0, 0);
            return;
        }

        if (!inBatch) {
            inBatch = true;
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GL11.glPushMatrix();
            GL11.glTranslated(x, y, z);
            GL11.glScaled(0.3, 0.3, 0.3);
            RenderHelper.disableStandardItemLighting();
        }
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightc % (float) 0x1_00_00,
            lightc / (float) 0x1_00_00);
        Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
    }

    private static void setupModelOffsetRandom(ItemStack stack) {
        final long seed;
        if (stack.isEmpty()) {
            seed = 137;
        } else {
            ResourceLocation regName = stack.getItem().getRegistryName();
            if (regName == null) {
                seed = 127;
            } else {
                int regNameSeed = regName.getResourceDomain().hashCode() ^ regName.getResourcePath().hashCode();
                seed = (regNameSeed & 0x7F_FF_FF_FF) | (((long) stack.getMetadata()) << 32);
            }
        }
        modelOffsetRandom.setSeed(seed);
    }

    private static int getStackModelCount(int stackCount) {
        if (stackCount > 1) {
            if (stackCount > 16) {
                if (stackCount > 32) {
                    if (stackCount > 48) {
                        return 5;
                    } else {
                        return 4;
                    }
                } else {
                    return 3;
                }
            } else {
                return 2;
            }
        } else {
            return 1;
        }
    }

    public static void endItemBatch() {
        if (inBatch) {
            inBatch = false;
            GL11.glPopMatrix();
        }
    }
}
