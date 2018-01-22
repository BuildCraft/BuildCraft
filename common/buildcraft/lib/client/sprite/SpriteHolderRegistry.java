/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.sprite;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.gson.JsonObject;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.render.ISprite;

import buildcraft.lib.client.resource.DataMetadataSection;
import buildcraft.lib.client.resource.MetadataLoader;
import buildcraft.lib.misc.SpriteUtil;

@SideOnly(Side.CLIENT)
public class SpriteHolderRegistry {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.sprite.holder");

    private static final Map<ResourceLocation, SpriteHolder> HOLDER_MAP = new HashMap<>();

    public static SpriteHolder getHolder(ResourceLocation location) {
        if (!HOLDER_MAP.containsKey(location)) {
            HOLDER_MAP.put(location, new SpriteHolder(location));
            if (DEBUG) {
                BCLog.logger.info("[lib.sprite.holder] Created a new sprite holder for " + location);
            }
        } else if (DEBUG) {
            BCLog.logger.info("[lib.sprite.holder] Returned existing sprite holder for " + location);
        }
        return HOLDER_MAP.get(location);
    }

    public static SpriteHolder getHolder(String location) {
        return getHolder(new ResourceLocation(location));
    }

    public static void onTextureStitchPre(TextureMap map) {
        for (SpriteHolder holder : HOLDER_MAP.values()) {
            holder.onTextureStitchPre(map);
        }
    }

    public static void exportTextureMap() {
        if (!DEBUG) {
            return;
        }
        TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
        GlStateManager.bindTexture(map.getGlTextureId());

        for (int l = 0; l < 4; l++) {
            int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, l, GL11.GL_TEXTURE_WIDTH);
            int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, l, GL11.GL_TEXTURE_HEIGHT);

            GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
            GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

            int totalSize = width * height;
            IntBuffer intbuffer = BufferUtils.createIntBuffer(totalSize);
            int[] aint = new int[totalSize];
            GL11.glGetTexImage(GL11.GL_TEXTURE_2D, l, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, intbuffer);
            intbuffer.get(aint);
            BufferedImage bufferedimage = new BufferedImage(width, height, 2);
            bufferedimage.setRGB(0, 0, width, height, aint, 0, width);

            try {
                ImageIO.write(bufferedimage, "png", new File("bc_spritemap_" + l + ".png"));
            } catch (IOException io) {
                BCLog.logger.warn(io);
            }
        }
    }

    public static void onTextureStitchPost() {
        if (DEBUG && Loader.instance().isInState(LoaderState.AVAILABLE)) {
            BCLog.logger.info("[lib.sprite.holder] List of registered sprites:");
            List<ResourceLocation> locations = new ArrayList<>(HOLDER_MAP.keySet());
            locations.sort(Comparator.comparing(ResourceLocation::toString));

            TextureAtlasSprite missing = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();

            for (ResourceLocation r : locations) {
                SpriteHolder sprite = HOLDER_MAP.get(r);
                TextureAtlasSprite stitched = sprite.sprite;
                String status = "  ";
                if (stitched == null) {
                    status += "(Sprite was registered too late)";
                } else if (missing.getMinU() == stitched.getMinU() && missing.getMinV() == stitched.getMinV()) {
                    status += "(Sprite did not exist in a resource pack)";
                }

                BCLog.logger.info("  - " + r + status);
            }
            BCLog.logger.info("[lib.sprite.holder] Total of " + HOLDER_MAP.size() + " sprites");
        }
    }

    /** Holds a reference to a {@link TextureAtlasSprite} that is automatically refreshed when the resource packs are
     * reloaded. As such you should store this in a static final field in a client-side class, and make sure that the
     * class is initialised before init. */
    @SideOnly(Side.CLIENT)
    public static class SpriteHolder implements ISprite {
        public final ResourceLocation spriteLocation;
        private TextureAtlasSprite sprite;
        private DataMetadataSection extraData = null;
        private boolean hasCalled = false;

        private SpriteHolder(ResourceLocation spriteLocation) {
            this.spriteLocation = spriteLocation;
        }

        protected void onTextureStitchPre(TextureMap map) {
            extraData = null;
            TextureAtlasSprite varSprite = AtlasSpriteVariants.createForConfig(spriteLocation);
            if (map.setTextureEntry(varSprite)) {
                sprite = varSprite;
            } else {
                sprite = map.getTextureExtry(varSprite.getIconName());
            }
        }

        private TextureAtlasSprite getSpriteChecking() {
            if (sprite == null & !hasCalled) {
                hasCalled = true;
                String warnText = "[lib.sprite.holder] Tried to use the sprite " + spriteLocation + " before it was stitched!";
                if (DEBUG) {
                    BCLog.logger.warn(warnText, new Throwable());
                } else {
                    BCLog.logger.warn(warnText);
                }
            }
            return sprite;
        }

        public TextureAtlasSprite getSprite() {
            return getSpriteChecking();
        }

        @Override
        public double getInterpU(double u) {
            TextureAtlasSprite s = getSpriteChecking();
            return s == null ? u : s.getMinU() + u * (s.getMaxU() - s.getMinU());
        }

        @Override
        public double getInterpV(double v) {
            TextureAtlasSprite s = getSpriteChecking();
            return s == null ? v : s.getMinV() + v * (s.getMaxV() - s.getMinV());
        }

        @Override
        public void bindTexture() {
            SpriteUtil.bindBlockTextureMap();
        }

        public DataMetadataSection getExtraData(boolean samePack) {
            if (extraData == null) {
                ResourceLocation actualLocation = SpriteUtil.transformLocation(spriteLocation);
                extraData = MetadataLoader.getData(actualLocation, samePack);
            }
            if (extraData == null) {
                extraData = new DataMetadataSection(new JsonObject());
            }
            return extraData;
        }
    }
}
