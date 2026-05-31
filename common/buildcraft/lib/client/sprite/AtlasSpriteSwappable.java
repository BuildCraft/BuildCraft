/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.sprite;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.renderer.texture.PngSizeInfo;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.Identifier;

import net.minecraftforge.fml.client.FMLClientHandler;

import buildcraft.lib.BCLibConfig;

/** Provides the basic implementation for */
public abstract class AtlasSpriteSwappable extends Sprite {
    private Sprite current;
    private boolean needsSwapping = true;

    public AtlasSpriteSwappable(String baseName) {
        super(baseName);
        if (!BCLibConfig.useSwappableSprites) {
            throw new IllegalStateException(
                "The user has disabled swappable sprites but some code still called it's constructor anyway!\n"
                    + "(Note that this is a *mod* bug, not a user configuration issue - there are legitimate reasons\n"
                    + "to disabled swappable sprites normally, like for optifine compat)");
        }
    }

    @Override
    public boolean hasAnimationMetadata() {
        return true;
    }

    @Override
    public void updateAnimation() {
        if (current == null) {
            copyFrom(MinecraftClient.getInstance().getTextureMapBlocks().getMissingSprite());
            return;
        }
        Profiler p = MinecraftClient.getInstance().getProfiler();
        // MAPPING: func_194340_a: Profiler.startSection
        p.func_194340_a(getClass()::getSimpleName);
        if (needsSwapping) {
            p.push("copy");
            current.copyFrom(this);
            p.pop();
        }
        if (current.hasAnimationMetadata() && BCLibConfig.enableAnimatedSprites) {
            p.push("update");
            p.push(getIconName());
            current.updateAnimation();
            p.pop();
            p.pop();
        } else if (needsSwapping) {
            p.push("swap");
            TextureUtil.uploadTextureMipmap(current.getFrameTextureData(0), current.getIconWidth(),
                current.getIconHeight(), current.getOriginX(), current.getOriginY(), false, false);
            p.pop();
        }
        needsSwapping = false;
        p.pop();
    }

    public boolean swapWith(Sprite other) {
        if (current != other && (current == null || other != null)) {
            current = other;
            if (width == 0) {
                this.width = other.getIconWidth();
                this.height = other.getIconHeight();
            }
            generateMipmaps(MinecraftClient.getInstance().gameSettings.mipmapLevels);
            needsSwapping = true;
            return true;
        }
        return false;
    }

    /** Actually loads the given location. Note that subclasses should override this, and possibly call
     * {@link #loadSprite(ResourceManager, String, Identifier, boolean)} to load all of the possible variants. */
    @Override
    public boolean load(ResourceManager manager, Identifier location,
        Function<Identifier, Sprite> textureGetter) {
        Sprite sprite = loadSprite(manager, super.getIconName(), location, true);
        if (sprite != null) {
            swapWith(sprite);
        }
        return false;
    }

    public static Sprite loadSprite(String name, Identifier location, boolean careIfMissing) {
        return loadSprite(MinecraftClient.getInstance().getResourceManager(), name, location, careIfMissing);
    }

    public static Sprite loadSprite(ResourceManager manager, String name, Identifier location,
        boolean careIfMissing) {
        // Load the initial variant
        Sprite sprite = makeAtlasSprite(new Identifier(name));
        try {
            // Copied almost directly from TextureMap.
            PngSizeInfo pngsizeinfo = PngSizeInfo.makeFromResource(manager.getResource(location));
            try (IResource iresource = manager.getResource(location)) {
                boolean flag = iresource.getMetadata("animation") != null;
                sprite.loadSprite(pngsizeinfo, flag);
                sprite.loadSpriteFrames(iresource, MinecraftClient.getInstance().gameSettings.mipmapLevels + 1);
                return sprite;
            }
        } catch (IOException io) {
            if (careIfMissing) {
                // Do the same as forge - track the missing texture for later rather than printing out the error.
                FMLClientHandler.instance().trackMissingTexture(location);
            }
            return null;
        }
    }

    @Override
    public boolean hasCustomLoader(ResourceManager manager, Identifier location) {
        return true;
    }

    @Override
    public void generateMipmaps(int level) {
        if (current != null) {
            current.generateMipmaps(level);
        }
    }

    // Overrides

    @Override
    public int getFrameCount() {
        if (current == null) {
            return 0;
        }
        return current.getFrameCount();
    }

    @Override
    public void copyFrom(Sprite from) {
        super.copyFrom(from);
        if (current == null) {
            current = from;
        } else {
            current.copyFrom(from);
        }
    }

    @Override
    public int[][] getFrameTextureData(int index) {
        if (current == null) {
            return new int[1][1];
        }
        return current.getFrameTextureData(index);
    }

    @Override
    public void setFramesTextureData(List<int[][]> newFramesTextureData) {
        // NO-OP
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + super.toString();
    }
}
