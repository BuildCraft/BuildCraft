package buildcraft.datagen.base;

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public abstract class BCBaseTextureGenerator implements DataProvider {
    protected static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    protected static final Logger LOGGER = LogManager.getLogger();
    protected final PackOutput packOutput;
    protected final ExistingFileHelper exFileHelper;

    protected BCBaseTextureGenerator(PackOutput packOutput, ExistingFileHelper exFileHelper) {
        this.packOutput = packOutput;
        this.exFileHelper = exFileHelper;
    }

    protected CompletableFuture<?> save(BufferedImage image, CachedOutput cache, Path path) throws IOException {
        return CompletableFuture.runAsync(() ->
        {
            try {
                ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
                HashingOutputStream hashingoutputstream = new HashingOutputStream(Hashing.sha1(), bytearrayoutputstream);
                ImageIO.write(image, "png", hashingoutputstream);

                cache.writeIfNeeded(path, bytearrayoutputstream.toByteArray(), hashingoutputstream.hash());
            } catch (IOException ioexception) {
                LOGGER.error("Failed to save file to {}", path, ioexception);
            }

        }, Util.backgroundExecutor());
    }
}
