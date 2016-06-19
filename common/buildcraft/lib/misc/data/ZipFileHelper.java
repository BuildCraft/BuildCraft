package buildcraft.lib.misc.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.google.common.collect.ImmutableSet;

import org.apache.commons.io.IOUtils;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.lib.nbt.NbtSquisher;

public class ZipFileHelper {
    private final Map<String, byte[]> entries = new HashMap<>();
    private final Map<String, String> comments = new HashMap<>();

    public ZipFileHelper() {}

    public ZipFileHelper(ZipInputStream zis) throws IOException {
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (entry.isDirectory()) continue;

            String name = entry.getName();
            comments.put(name, entry.getComment());
            entries.put(name, IOUtils.toByteArray(zis));
        }
    }

    public void write(ZipOutputStream zos) throws IOException {
        for (String key : entries.keySet()) {
            writeEntry(zos, key);
        }
    }

    private void writeEntry(ZipOutputStream zos, String entryName) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        entry.setComment(comments.get(entryName));
        zos.putNextEntry(entry);
        zos.write(entries.get(entryName));
        zos.closeEntry();
    }

    public ImmutableSet<String> getKeys() {
        return ImmutableSet.copyOf(entries.keySet());
    }

    public void addNbtEntry(String name, String comment, NBTTagCompound nbt, int type) {
        comments.put(name, comment);
        entries.put(name, NbtSquisher.squish(nbt, type));
    }

    public void addTextEntry(String name, String comment, String text) {
        comments.put(name, comment);
        entries.put(name, text.getBytes(StandardCharsets.UTF_8));
    }

    public NBTTagCompound getNbtEntry(String name) throws IOException {
        byte[] bytes = entries.get(name);
        if (bytes == null) {
            throw new IOException("No bytes for entry " + name);
        }
        return NbtSquisher.expand(bytes);
    }

    public String getTextEntry(String name) throws IOException {
        byte[] bytes = entries.get(name);
        if (bytes == null) {
            throw new IOException("No bytes for entry " + name);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
