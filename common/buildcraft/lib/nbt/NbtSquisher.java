package buildcraft.lib.nbt;

import java.io.IOException;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 
 */
public class NbtSquisher {
    /* Defines a compression program that can turn large, mostly-similar, dense, NBTTagCompounds into much smaller
     * variants.
     * 
     * Compression has the following steps:
     * 
     * - 1: */

    public static byte[] squish(NBTTagCompound nbt) {
        NBTSquishMap map = new NBTSquishMap();
        map.addTag(nbt);
        ByteBuf buf = Unpooled.buffer();
        if (NBTSquishDebugging.debug) {
            buf = new PrintingByteBuf(buf);
        }

        NBTSquishMapWriter.write(map, buf);
        WrittenType type = map.getWrittenType();
        type.writeIndex(buf, map.indexOfTag(nbt));
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        NBTSquishDebugging.log(() -> "\nUsed type " + type + " (as there are " + map.size() + " object types)");
        return bytes;
    }

    public static NBTTagCompound expand(byte[] bytes) throws IOException, IndexOutOfBoundsException {
        ByteBuf buf = Unpooled.copiedBuffer(bytes);
        NBTSquishMap map = NBTSquishMapReader.read(buf);
        WrittenType type = map.getWrittenType();
        int index = type.readIndex(buf);
        NBTBase nbt = map.getTagForReading(index);
        return (NBTTagCompound) nbt;
    }
}
