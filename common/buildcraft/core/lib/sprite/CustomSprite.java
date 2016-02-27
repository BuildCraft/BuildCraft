package buildcraft.core.lib.sprite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import buildcraft.core.lib.sprite.SpriteBuilder.ISpriteAction;

public class CustomSprite extends TextureAtlasSprite {
    private final List<ISpriteAction> actions;

    public CustomSprite(String spriteName, List<ISpriteAction> actions) {
        super(spriteName);
        this.actions = actions;
    }

    @Override
    public boolean hasCustomLoader(IResourceManager manager, ResourceLocation location) {
        return true;
    }

    private static boolean isPowerOfTwo(int i) {
        return i != 0 && (i & i - 1) == 0;
    }

    @Override
    public boolean load(IResourceManager manager, ResourceLocation location) {
        int width = MathHelper.calculateLogBaseTwo(16);
        for (ISpriteAction action : actions) {
            int needed = action.getMinWidth(manager);
            if (!isPowerOfTwo(needed)) throw new IllegalArgumentException(needed + " was not a power of two!");
            int p2 = MathHelper.calculateLogBaseTwo(needed);
            if (p2 > width) {
                width = p2;
            }
        }
        int height = (int) Math.pow(2, width);

        BufferedImage image = new BufferedImage(height, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setClip(0, 0, height, height);
        graphics.setBackground(new Color(0, 0, 0, 0));
        graphics.clearRect(0, 0, height, height);

        for (ISpriteAction action : actions) {
            action.apply(graphics, manager);
        }

        BufferedImage[] images = new BufferedImage[Minecraft.getMinecraft().gameSettings.mipmapLevels + 1];
        images[0] = image;
        try {
            loadSprite(images, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public void register(TextureMap map) {
        map.setTextureEntry(this.getIconName(), this);
    }
}
