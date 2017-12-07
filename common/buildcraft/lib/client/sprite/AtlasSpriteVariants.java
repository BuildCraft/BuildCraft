/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.sprite;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntSupplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;

import buildcraft.lib.BCLibConfig;
import buildcraft.lib.client.reload.IReloadable;
import buildcraft.lib.client.reload.ReloadManager;
import buildcraft.lib.client.reload.ReloadSource;
import buildcraft.lib.client.reload.ReloadUtil;
import buildcraft.lib.client.reload.SourceType;
import buildcraft.lib.misc.SpriteUtil;

/** A type of {@link AtlasSpriteSwappable} that will switch between multiple different */
public class AtlasSpriteVariants extends AtlasSpriteSwappable implements IReloadable {
    public static final IVariantType VARIANT_COLOUR_BLIND =
        loc -> ImmutableList.of(loc, new ResourceLocation(loc.getResourceDomain(), loc.getResourcePath() + "_cb"));
    public static final IntSupplier INDEX_COLOUR_BLIND = () -> BCLibConfig.colourBlindMode ? 1 : 0;

    private final List<ResourceLocation> variantNames;
    private final IntSupplier currentIndexFunction;
    private final TextureAtlasSprite[] variants;
    private int currentIndex = -1;

    public AtlasSpriteVariants(List<ResourceLocation> variantNames, IntSupplier currentIndexFunction) {
        super(variantNames.get(0).toString());
        if (variantNames.isEmpty()) {
            throw new IllegalArgumentException("Not enough names!");
        }
        this.variantNames = processNames(variantNames);
        this.variants = new TextureAtlasSprite[variantNames.size()];
        this.currentIndexFunction = currentIndexFunction;
    }

    public AtlasSpriteVariants(ResourceLocation baseName, IVariantType variants, IntSupplier currentIndex) {
        this(variants.getAllPossibleVariants(baseName), currentIndex);
    }

    private static List<ResourceLocation> processNames(List<ResourceLocation> names) {
        ImmutableList.Builder<ResourceLocation> builder = ImmutableList.builder();
        for (ResourceLocation loc : names) {
            builder.add(SpriteUtil.transformLocation(loc));
        }
        return builder.build();
    }

    public static TextureAtlasSprite createForConfig(ResourceLocation baseName) {
        if (BCLibConfig.useSwappableSprites) {
            AtlasSpriteVariants sprite = new AtlasSpriteVariants(baseName, VARIANT_COLOUR_BLIND, INDEX_COLOUR_BLIND);
            ReloadSource to = new ReloadSource(SpriteUtil.transformLocation(baseName), SourceType.SPRITE);
            ReloadManager manager = ReloadManager.INSTANCE;
            manager.addDependency(ReloadManager.CONFIG_COLOUR_BLIND, sprite, to);
            manager.addDependency(new ReloadSource(sprite.variantNames.get(0), SourceType.FILE), sprite, to);
            manager.addDependency(new ReloadSource(sprite.variantNames.get(1), SourceType.FILE), sprite, to);
            return sprite;
        } else {
            int index = INDEX_COLOUR_BLIND.getAsInt();
            ResourceLocation location = VARIANT_COLOUR_BLIND.getAllPossibleVariants(baseName).get(index);
            return new AtlasSpriteDirect(location.toString());
        }
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location,
        Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
        for (int i = 0; i < variantNames.size(); i++) {
            ResourceLocation loc = variantNames.get(i);
            variants[i] = loadSprite(manager, getIconName(), loc, i == 0);
        }
        currentIndex = -1;
        reload(ImmutableSet.of());
        return false;
    }

    @Override
    public boolean reload(Set<ReloadSource> changed) {
        if (!changed.isEmpty()) {
            for (int i = 0; i < variantNames.size(); i++) {
                ResourceLocation loc = variantNames.get(i);
                if (ReloadUtil.getSourceTypesFor(changed, loc).contains(SourceType.FILE)) {
                    TextureAtlasSprite s = loadSprite(getIconName(), loc, i == 0);
                    if (s != null) {
                        if (s.getIconWidth() == width && s.getIconHeight() == height) {
                            variants[i] = s;
                        } else {
                            BCLog.logger.warn(
                                "Unable to reload " + loc + " as the new sprite was a different width and height!");
                        }
                    }
                }
            }
        }
        currentIndex = currentIndexFunction.getAsInt();
        if (currentIndex < 0 || currentIndex >= variants.length) {
            currentIndex = 0;
        }
        TextureAtlasSprite sprite = variants[currentIndex];
        if (sprite == null) {
            sprite = variants[0];
            currentIndex = 0;
        }
        return swapWith(sprite);
    }

    public interface IVariantType {
        List<ResourceLocation> getAllPossibleVariants(ResourceLocation location);
    }
}
