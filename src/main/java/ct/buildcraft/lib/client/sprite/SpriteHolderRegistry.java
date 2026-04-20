/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.sprite;

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

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import ct.buildcraft.api.core.BCDebugging;
import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.lib.misc.SpriteUtil;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.ModLoader;

@OnlyIn(Dist.CLIENT)
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

    public static void onTextureStitchPre(TextureStitchEvent.Pre event) {
        for (SpriteHolder holder : HOLDER_MAP.values()) {
            holder.onTextureStitchPre(event);
        }
    }

    public static void exportTextureMap() {
        if (!DEBUG) {
            return;
        }
        TextureAtlas map = Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS);
        GlStateManager._bindTexture(map.getId());

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
                BCLog.logger.warn(io.getLocalizedMessage());
            }
        }
    }

    public static void onTextureStitchPost(TextureStitchEvent.Post event) {
        if (DEBUG&&ModLoader.isLoadingStateValid()) {
            BCLog.logger.info("[lib.sprite.holder] List of registered sprites:");
            List<ResourceLocation> locations = new ArrayList<>();
            locations.addAll(HOLDER_MAP.keySet());
            locations.sort(Comparator.comparing(ResourceLocation::toString));
            TextureAtlas manager = event.getAtlas();
            TextureAtlasSprite missing = manager.getSprite(MissingTextureAtlasSprite.getLocation());
            
            
            for (ResourceLocation r : locations) {
                SpriteHolder sprite = HOLDER_MAP.get(r);
                TextureAtlasSprite stitched = manager.getSprite(r);
                sprite.sprite = stitched;
                String status = "  ";
                if (missing.getU0() == stitched.getU0() && missing.getV0() == stitched.getV0()) {
                    status += "fail to load sprite "+r +" ,using missingno instead";
                }

                BCLog.logger.info("  - " + r + status);
            }
            BCLog.logger.info("[lib.sprite.holder] Total of " + HOLDER_MAP.size() + " sprites");
        }
    }

    /** Holds a reference to a {@link TextureAtlasSprite} that is automatically refreshed when the resource packs are
     * reloaded. As such you should store this in a static final field in a client-side class, and make sure that the
     * class is initialised before init. */
    @OnlyIn(Dist.CLIENT)
    public static class SpriteHolder implements ISprite {
        public final ResourceLocation spriteLocation;
        private TextureAtlasSprite sprite;
        private boolean hasCalled = false;

        private SpriteHolder(ResourceLocation spriteLocation) {
            this.spriteLocation = spriteLocation;
        }

        protected void onTextureStitchPre(TextureStitchEvent.Pre event) {
        	event.addSprite(spriteLocation);
        }

        private TextureAtlasSprite getSpriteChecking() {
            if (sprite == null & !hasCalled) {
                hasCalled = true;
                Minecraft mc = Minecraft.getInstance();
                sprite = mc.getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(spriteLocation);
/*                String warnText = "[lib.sprite.holder] Tried to use the sprite " + spriteLocation + " before it was stitched!";
                if (DEBUG) {
                    BCLog.logger.warn(warnText, new Throwable());
                } else {
                    BCLog.logger.warn(warnText);
                }*/
            }
            return sprite;
        }

        public TextureAtlasSprite getSprite() {
            return getSpriteChecking();
        }

        @Override
        public float getInterpU(double u) {
            TextureAtlasSprite s = getSpriteChecking();
            return s == null ? (float)u : s.getU(u*16);
        }

        @Override
        public float getInterpV(double v) {
            TextureAtlasSprite s = getSpriteChecking();
            return s == null ? (float)v : s.getV(v*16);
        }

        @Override
        public void bindTexture() {
            SpriteUtil.bindBlockTextureMap();
        }

    }
}
