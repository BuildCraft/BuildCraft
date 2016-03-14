package buildcraft.core.lib.client.sprite;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import com.google.common.collect.Lists;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class SpriteBuilder {
    private final String name;
    private final List<ISpriteAction> buildingActions = Lists.newArrayList();

    public SpriteBuilder(String name) {
        this.name = name;
    }

    /** Performs simple addition of the current image and a new image, replacing old pixels with the new image if the
     * new image's alpha channel is greater than the given value. */
    public SpriteBuilder addSprite(String location, final int minAlpha) {
        final ResourceLocation resLoc = new ResourceLocation(location);
        buildingActions.add(new ISpriteAction() {
            @Override
            public void apply(Graphics2D original, IResourceManager resourceManager) {
                BufferedImage image = getImage(resourceManager, resLoc);
                int multiplier = original.getClipBounds().width / image.getWidth();
                for (int x = 0; x < image.getWidth(); x++) {
                    for (int y = 0; y < image.getHeight(); y++) {
                        int pixel = image.getRGB(x, y);
                        int alpha = (pixel >> 24) & 0xFF;
                        if (alpha < minAlpha) continue;
                        int red = (pixel >> 16) & 0xFF;
                        int green = (pixel >> 8) & 0xFF;
                        int blue = pixel & 0xFF;
                        original.setColor(new Color(red, green, blue));
                        original.fillRect(x * multiplier, y * multiplier, multiplier, multiplier);
                    }
                }
            }

            @Override
            public int getMinWidth(IResourceManager resourceManager) {
                return SpriteBuilder.getMinWidth(resourceManager, resLoc);
            }
        });
        return this;
    }

    public CustomSprite build() {
        return new CustomSprite(name, buildingActions);
    }

    public static BufferedImage getImage(IResourceManager manager, ResourceLocation loc) {
        try {
            String domain = loc.getResourceDomain();
            String path = loc.getResourcePath();
            if (!path.startsWith("textures/")) path = "textures/" + path;
            if (!path.endsWith(".png")) path = path + ".png";
            ResourceLocation altered = new ResourceLocation(domain, path);

            IResource res = manager.getResource(altered);
            BufferedImage image = ImageIO.read(res.getInputStream());
            return image;
        } catch (IOException e) {
            if (loc == TextureMap.LOCATION_MISSING_TEXTURE) throw new RuntimeException("Unable to load the missing texture!", e);
            return getImage(manager, TextureMap.LOCATION_MISSING_TEXTURE);
        }
    }

    public static int getMinWidth(IResourceManager manager, ResourceLocation loc) {
        BufferedImage img = getImage(manager, loc);
        if (img.getWidth() != img.getHeight()) throw new IllegalArgumentException("The width must be the same as the height!");
        return img.getWidth();
    }

    public interface ISpriteAction {
        int getMinWidth(IResourceManager resourceManager);

        void apply(Graphics2D original, IResourceManager resourceManager);
    }
}
