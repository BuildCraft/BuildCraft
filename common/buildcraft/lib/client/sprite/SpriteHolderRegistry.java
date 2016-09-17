package buildcraft.lib.client.sprite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
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

    public static void onTextureStitchPost() {
        if (DEBUG && Loader.instance().isInState(LoaderState.AVAILABLE)) {
            BCLog.logger.info("[lib.sprite.holder] List of registered sprites:");
            List<ResourceLocation> locations = new ArrayList<>();
            locations.addAll(HOLDER_MAP.keySet());
            locations.sort((a, b) -> a.toString().compareTo(b.toString()));

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

    @SideOnly(Side.CLIENT)
    public static class SpriteHolder implements ISprite {
        public final ResourceLocation spriteLocation;
        private TextureAtlasSprite sprite;
        private boolean hasCalled = false;

        private SpriteHolder(ResourceLocation spriteLocation) {
            this.spriteLocation = spriteLocation;
        }

        protected void onTextureStitchPre(TextureMap map) {
            sprite = map.registerSprite(spriteLocation);
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
            return s == null ? u : s.getInterpolatedU(u * 16);
        }

        @Override
        public double getInterpV(double v) {
            TextureAtlasSprite s = getSpriteChecking();
            return s == null ? v : s.getInterpolatedV(v * 16);
        }

        @Override
        public void bindTexture() {
            SpriteUtil.bindBlockTextureMap();
        }
    }
}
