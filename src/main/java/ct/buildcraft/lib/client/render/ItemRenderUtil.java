/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.render;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.lwjgl.opengl.GL11;

import ct.buildcraft.api.core.EnumPipePart;
import ct.buildcraft.lib.BCLibConfig;
import ct.buildcraft.lib.client.model.MutableQuad;
import ct.buildcraft.lib.misc.ItemStackKey;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

@OnlyIn(Dist.CLIENT)
public class ItemRenderUtil {

    private static final LoadingCache<ItemStackKey, Integer> glListCache;

    private static final Random modelOffsetRandom = new Random(0);

    private static final ItemEntity dummyEntityItem = null;//new ItemEntity(null);
/*    private static final ItemRenderer customItemRenderer =
        new ItemRenderer(Minecraft.getInstance().getModelManager(), Minecraft.getInstance().getItemRenderer()) {
            @Override
            public boolean shouldSpreadItems() {
                return false;
            }

            @Override
            public boolean shouldBob() {
                return false;
            }
        };*/
    static {
        glListCache = CacheBuilder.newBuilder()//
            .expireAfterAccess(40, TimeUnit.SECONDS)//
            .removalListener(ItemRenderUtil::onStackRemove)//
            .build(CacheLoader.from(ItemRenderUtil::makeItemGlList));
    }

    private static Integer makeItemGlList(ItemStackKey item) {
 /*   	Entity
        int list = GLAllocation.generateDisplayLists(1);
        GL11.glNewList(list, GL11.GL_COMPILE);
        renderItemImpl(0, 0, 0, item.baseStack);
        GL11.glEndList();
        return list;*/
    	return null;
    }

    private static void onStackRemove(RemovalNotification<ItemStackKey, Integer> notification) {
        Integer val = notification.getValue();
        if (val != null) {
//            GLAllocation.deleteDisplayLists(val);
        }
    }

    private static void renderItemImpl(double x, double y, double z, ItemStack stack) {
/*        GL11.glPushMatrix();
        GL11.glTranslated(0, -0.2, 0);
        GL11.glScaled(0.9, 0.9, 0.9);

        // This is broken - some stacks render too big but some render way too small.
        // Also not all stacks are centered :/

        if (stack.getItem() instanceof BlockItem) {
            dummyEntityItem.hoverStart = 0;
        } else {
            // Items are rotated by 45 degrees
            dummyEntityItem.hoverStart = (float) (45 * Math.PI / 180);
        }

        dummyEntityItem.setItem(stack);
        customItemRenderer.doRender(dummyEntityItem, x, y, z, 0, 0);

        GL11.glPopMatrix();*/
    }

    // Batch item rendering

    private static boolean inBatch = false;

    /** Used to render a lot of items in sequential order. Assumes that you don't change the glstate inbetween calls.
     * You must call {@link #endItemBatch()} after your have rendered all of the items. */
    public static void renderItemStack(double x, double y, double z, ItemStack stack, int lightc, Direction dir,
    		VertexConsumer bb) {
        renderItemStack(x, y, z, stack, stack.getCount(), lightc, dir, bb);
    }

    /** Used to render a lot of items in sequential order. Assumes that you don't change the glstate inbetween calls.
     * You must call {@link #endItemBatch()} after your have rendered all of the items. */
    public static void renderItemStack(double x, double y, double z, ItemStack stack, int stackCount, int lightc,
        Direction dir, VertexConsumer bb) {
        if (stack.isEmpty()) {
            return;
        }
        try {
            renderItemStackInternal(x, y, z, stack, stackCount, lightc, dir, bb);
        } catch (Throwable exception) {
            CrashReport report = CrashReport.forThrowable(exception, "Rendering Item Stack");
            CrashReportCategory category = report.addCategory("Item being rendered");
            category.setDetail("Stack Count", stackCount);
            category.setDetail("Item Class", () -> "" + stack.getItem().getClass());
            category.setDetail("Item ID", () -> "" + ForgeRegistries.ITEMS.getKey(stack.getItem()));
//            category.setDetail("Item Meta", () -> "" + stack.());
            category.setDetail("Item NBT", () -> "" + stack.getTag());
            throw new ReportedException(report);
        }
    }

    private static void renderItemStackInternal(
        double x, double y, double z, ItemStack stack, int stackCount, int lightc, Direction dir, VertexConsumer bb
    ) {
        if (dir == null) {
            dir = Direction.EAST;
        }
        dir = BCLibConfig.rotateTravelingItems.changeFacing(dir);

        BakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(stack);
        model = model.getOverrides().resolve(model, stack, null, null, 0);
        boolean requireGl = stack.hasFoil() || model.isCustomRenderer();

 /*       if (bb != null && !requireGl) {

            final int itemModelCount = getStackModelCount(stackCount);

            if (itemModelCount > 1) {
                setupModelOffsetRandom(stack);
            }
            
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().

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
                    for (BakedQuad quad : model.getQuads(null, part.face, RandomSource.create())) {
                        q.fromBakedItem(quad);
                        q.translated(-0.5, -0.5, -0.5);
                        q.scaled(scale);
                        q.rotate(Direction.SOUTH, dir, 0, 0, 0);
                        if (quad.isTinted()) {
                            int colour =
                                Minecraft.getInstance().getItemColors().getColor(stack, quad.getTintIndex());
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
//            Minecraft.getInstance().getModelManager().tex(InventoryMenu.BLOCK_ATLAS);
            GL11.glPushMatrix();
            GL11.glTranslated(x, y, z);
            GL11.glScaled(0.3, 0.3, 0.3);
            RenderHelper.disableStandardItemLighting();
        }
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightc % (float) 0x1_00_00,
            lightc / (float) 0x1_00_00);
        Minecraft.getInstance().getItemRenderer().render(stack, model);*/
    }

    private static void setupModelOffsetRandom(ItemStack stack) {
        final long seed;
        if (stack.isEmpty()) {
            seed = 137;
        } else {
            ResourceLocation regName = new ResourceLocation(stack.getItem().getDescriptionId());
            if (regName == null) {
                seed = 127;
            } else {
                int regNameSeed = regName.getNamespace().hashCode() ^ regName.getPath().hashCode();
                seed = (regNameSeed & 0x7F_FF_FF_FF) | (((long) stack.getTag().hashCode()) << 32);
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
