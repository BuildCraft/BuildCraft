/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.model;

import buildcraft.lib.misc.SpriteUtil;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/** Provides a simple way of rendering an item model with just a list of quads. This provides some transforms to use
 * that make it simple to render as a block, item or tool (todo) */
@SuppressWarnings("deprecation")
public class ModelItemSimple implements IBakedModel {
    public static final ItemCameraTransforms TRANSFORM_DEFAULT = ItemCameraTransforms.NO_TRANSFORMS;
    public static final ItemCameraTransforms TRANSFORM_BLOCK;
    public static final ItemCameraTransforms TRANSFORM_PLUG_AS_ITEM;
    public static final ItemCameraTransforms TRANSFORM_PLUG_AS_ITEM_BIGGER;
    public static final ItemCameraTransforms TRANSFORM_PLUG_AS_BLOCK;
    public static final ItemCameraTransforms TRANSFORM_ITEM;
    // TODO: TRANSFORM_TOOL

    static {
        // Values taken from "minecraft:models/core/core.json"
        ItemTransformVec3f thirdp_left = def(75, 45, 0, 0, 2.5, 0, 0.375);
        ItemTransformVec3f thirdp_right = def(75, 225, 0, 0, 2.5, 0, 0.375);
        ItemTransformVec3f firstp_left = def(0, 225, 0, 0, 0, 0, 0.4);
        ItemTransformVec3f firstp_right = def(0, 45, 0, 0, 0, 0, 0.4);
        ItemTransformVec3f head = def(0, 0, 0, 0, 0, 0, 1);
        ItemTransformVec3f gui = def(30, 225, 0, 0, 0, 0, 0.625);
        ItemTransformVec3f ground = def(0, 0, 0, 0, 3, 0, 0.25);
        ItemTransformVec3f fixed = def(0, 0, 0, 0, 0, 0, 0.5);
        TRANSFORM_BLOCK =
                new ItemCameraTransforms(thirdp_left, thirdp_right, firstp_left, firstp_right, head, gui, ground, fixed);

        ItemTransformVec3f item_head = def(0, 0, 0, 0, 0, 0, 1);
        ItemTransformVec3f item_gui = def(0, 90, 0, 0, 0, 0, 1);
        ItemTransformVec3f item_ground = def(0, 0, 0, 0, 3, 0, 0.5);
        ItemTransformVec3f item_fixed = def(0, 0, 0, 0, 0, 0, 0.85);
        firstp_left = def(0, 225, 0, 0, 0, -4, 0.4);
        firstp_right = def(0, 45, 0, 0, 0, -4, 0.4);
        TRANSFORM_PLUG_AS_ITEM = new ItemCameraTransforms(thirdp_left, thirdp_right, firstp_left, firstp_right,
                item_head, item_gui, item_ground, item_fixed);
        TRANSFORM_PLUG_AS_ITEM_BIGGER = scale(TRANSFORM_PLUG_AS_ITEM, 1.8);

        thirdp_left = def(75, 45, 0, 0, 2.5, 0, 0.375);
        thirdp_right = def(75, 225, 0, 0, 2.5, 0, 0.375);
        firstp_left = def(0, 45, 0, 0, 0, 0, 0.4);
        firstp_right = def(0, 225, 0, 0, 0, 0, 0.4);
        gui = def(30, 135, 0, -3, 1.5, 0, 0.625);
        TRANSFORM_PLUG_AS_BLOCK =
                new ItemCameraTransforms(thirdp_left, thirdp_right, firstp_left, firstp_right, head, gui, ground, fixed);

        ground = def(0, 0, 0, 0, 2, 0, 0.5);
        head = def(0, 180, 0, 0, 13, 7, 1);
        thirdp_right = def(0, 0, 0, 0, 3, 1, 0.55);
        firstp_right = def(0, -90, 25, 1.13, 3.2, 1.13, 0.68);
        thirdp_left = thirdp_right;
        firstp_left = firstp_right;
        fixed = def(0, 180, 0, 0, 0, 0, 1);
        gui = def(0, 0, 0, 0, 0, 0, 1);
        TRANSFORM_ITEM =
                new ItemCameraTransforms(thirdp_left, thirdp_right, firstp_left, firstp_right, head, gui, ground, fixed);
    }

    private static ItemCameraTransforms scale(ItemCameraTransforms from, double by) {
        ItemTransformVec3f thirdperson_left = scale(from.thirdPersonLeftHand, by);
        ItemTransformVec3f thirdperson_right = scale(from.thirdPersonRightHand, by);
        ItemTransformVec3f firstperson_left = scale(from.firstPersonLeftHand, by);
        ItemTransformVec3f firstperson_right = scale(from.firstPersonRightHand, by);
        ItemTransformVec3f head = scale(from.head, by);
        ItemTransformVec3f gui = scale(from.gui, by);
        ItemTransformVec3f ground = scale(from.ground, by);
        ItemTransformVec3f fixed = scale(from.fixed, by);
        return new ItemCameraTransforms(thirdperson_left, thirdperson_right, firstperson_left, firstperson_right, head,
                gui, ground, fixed);
    }

    private static ItemTransformVec3f scale(ItemTransformVec3f from, double by) {

        float scale = (float) by;
        Vector3f nScale = new Vector3f(from.scale.x(), from.scale.y(), from.scale.z());
        nScale.mul(scale);

        return new ItemTransformVec3f(from.rotation, from.translation, nScale);
    }

    private static ItemTransformVec3f translate(ItemTransformVec3f from, double dx, double dy, double dz) {
        Vector3f nTranslation = new Vector3f(from.translation.x(), from.translation.y(), from.translation.z());
        nTranslation.add((float) dx, (float) dy, (float) dz);
        return new ItemTransformVec3f(from.rotation, nTranslation, from.scale);
    }

    private static ItemTransformVec3f def(double rx, double ry, double rz, double tx, double ty, double tz, double scale) {
        return def((float) rx, (float) ry, (float) rz, (float) tx, (float) ty, (float) tz, (float) scale);
    }

    private static ItemTransformVec3f def(float rx, float ry, float rz, float tx, float ty, float tz, float scale) {
        Vector3f rot = new Vector3f(rx, ry, rz);
        Vector3f translate = new Vector3f(tx / 16f, ty / 16f, tz / 16f);
        return new ItemTransformVec3f(rot, translate, new Vector3f(scale, scale, scale));
    }

    private final boolean isGui3d;
    private final List<BakedQuad> quads;
    private final TextureAtlasSprite particle;
    private final ItemCameraTransforms transforms;

    public ModelItemSimple(List<BakedQuad> quads, ItemCameraTransforms transforms, boolean isGui3d) {
        this.quads = quads == null ? ImmutableList.of() : quads;
        this.isGui3d = isGui3d;
        if (quads.isEmpty()) {
//            particle = Minecraft.getInstance().getTextureMapBlocks().getMissingSprite();
            this.particle = SpriteUtil.missingSprite();
        } else {
            this.particle = quads.get(0).getSprite();
        }
        this.transforms = transforms;
    }

    @Override
//    public List<BakedQuad> getQuads(BlockState state, Direction side, long rand)
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random rand) {
        return face == null ? quads : ImmutableList.of();
    }

    @Override
//    public boolean isAmbientOcclusion()
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return isGui3d;
    }

    @Override
//    public boolean isBuiltInRenderer()
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
//    public TextureAtlasSprite getParticleTexture()
    public TextureAtlasSprite getParticleIcon() {
        return particle;
    }

    @Override
//    public ItemCameraTransforms getItemTransforms()
    public ItemCameraTransforms getTransforms() {
        return transforms;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }
}
