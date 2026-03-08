package buildcraft.datagen.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public abstract class BCBaseTextureGenerator implements DataProvider {
    protected static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    protected static final Logger LOGGER = LogManager.getLogger();
    protected final DataGenerator generator;
    protected final ExistingFileHelper exFileHelper;

    protected BCBaseTextureGenerator(DataGenerator gen, ExistingFileHelper exFileHelper) {
        this.generator = gen;
        this.exFileHelper = exFileHelper;
    }

    protected void save(BufferedImage image, HashCache cache, Path path) throws IOException {
        String s = image.toString();
        String s1 = SHA1.hashUnencodedChars(s).toString();
        if (!Objects.equals(cache.getHash(path), s1) || !Files.exists(path)) {
            Files.createDirectories(path.getParent());

            ImageIO.write(image, "png", path.toFile());
        }

        cache.putNew(path, s1);
    }
}
