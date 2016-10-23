package buildcraft.lib.client.guide.font;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;

import net.minecraft.util.math.MathHelper;

import buildcraft.api.core.BCDebugging;

public class SimpleTextureMap {
    private static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.texturemap");

    private int glId;
    private List<BufferedImage> rawTextures;
    private List<SimpleTexture> uploadedTextures = new ArrayList<>();

    public int loadTexture(int[][] texture) {
        BufferedImage img = new BufferedImage(texture.length, texture[0].length, BufferedImage.TYPE_4BYTE_ABGR);
        WritableRaster raster = img.getRaster();
        for (int x = 0; x < texture.length; x++) {
            raster.setPixels(x, 0, 1, texture[x].length, texture[x]);
        }
        rawTextures.add(img);
        return rawTextures.size() - 1;
    }

    public void stitchAndUpload() {
        BufferedImage stitched = stitch();
        dump(stitched);
        rawTextures.clear();
        upload(stitched);
    }

    private BufferedImage stitch() {
        Map<BufferedImage, Integer> map = new IdentityHashMap<>();
        int maxWidth = 0;
        int maxHeight = 0;
        int textureCount = 0;
        List<BufferedImage> sorted = new ArrayList<>();
        for (BufferedImage raw : rawTextures) {
            maxWidth = Math.max(maxWidth, raw.getWidth());
            maxHeight = Math.max(maxHeight, raw.getHeight());
            map.put(raw, textureCount++);
            sorted.add(raw);
        }
        Collections.sort(sorted, new Comparator<BufferedImage>() {
            @Override
            public int compare(BufferedImage o1, BufferedImage o2) {
                if (o1.getHeight() != o2.getHeight()) {
                    return 100 * (o1.getHeight() - o2.getHeight());
                }
                if (o1.getWidth() != o2.getWidth()) {
                    return o1.getWidth() - o2.getWidth();
                }
                return 0;
            }
        });
        int sqrt = MathHelper.ceiling_double_int(Math.sqrt(textureCount));
        BufferedImage stitched = new BufferedImage(maxWidth * sqrt, maxHeight * sqrt, BufferedImage.TYPE_4BYTE_ABGR);
        WritableRaster raster = stitched.getRaster();
        for (int x = 0; x < sqrt; x++) {
            for (int y = 0; y < sqrt; y++) {
                BufferedImage raw = rawTextures.get(x * sqrt + y);
                raster.setDataElements(x * maxWidth, y * maxHeight, raw.getRaster());
            }
        }
        return stitched;
    }

    private static void dump(BufferedImage stitched) {
        output(stitched, "at" + System.currentTimeMillis());
    }

    private static void output(BufferedImage bufferedImage, String string) {
        if (DEBUG) {
            File loc = new File("./bc-tex/tex-map/");
            loc = new File(loc.getAbsolutePath());
            loc.mkdirs();
            loc = new File(loc, string + ".png");
            try {
                ImageIO.write(bufferedImage, "png", loc);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void upload(BufferedImage img) {

    }

    public SimpleTexture get(int id) {
        return uploadedTextures.get(id);
    }

    public static class SimpleTexture {

    }
}
