package buildcraft.builders.bpt;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import buildcraft.lib.nbt.NbtSquisher;

public enum PerSaveBptStorage {
    INSTANCE;

    private static final List<NBTTagCompound> blueprints = new ArrayList<>();

    public static int storeNBT(NBTTagCompound nbt) {
        if (nbt == null) {
            throw new NullPointerException();
        }
        for (int i = 0; i < blueprints.size(); i++) {
            NBTTagCompound stored = blueprints.get(i);
            if (stored.equals(nbt)) {
                return i;
            }
        }
        blueprints.add(nbt);
        return blueprints.size() - 1;
    }

    public static NBTTagCompound retrieveNbt(int id) {// TODO: Test this stuff!
        if (id < 0) {
            return null;
        }
        if (id >= blueprints.size()) {
            return null;
        }
        return blueprints.get(id);
    }

    public static void onServerStart(FMLServerStartingEvent event) {
        MinecraftServer server = event.getServer();
        File file = getSaveFile(server);

        try (GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(file))) {
            DataInputStream dis = new DataInputStream(gzip);
            int length = dis.readInt();
            byte[] squished = new byte[length];
            gzip.read(squished);
            NBTTagCompound main = NbtSquisher.expand(squished);

            // We have the data, now lets fully read the blueprints
            blueprints.clear();
            NBTTagList list = main.getTagList("list", Constants.NBT.TAG_COMPOUND);
            for (int idx = 0; idx < list.tagCount(); idx++) {
                NBTTagCompound entry = list.getCompoundTagAt(idx);
                blueprints.add(entry);
            }
        } catch (FileNotFoundException fnfe) {
            // Thats fine, maybe their wasn't any blueprints previously
        } catch (IOException io) {
            // BIG problem, we might not be able to recover from this safely
            String error = "Failed to load the blueprint file!\n" +//
                "Please report this to https://github.com/BuildCraft/BuildCraft/issues";
            throw new Error(error, io);
        }
    }

    public static void onServerStopping() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        File file = getSaveFile(server);

        // Put the blueprints down into one massive NBTTagCompound
        NBTTagCompound main = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        main.setTag("list", list);
        for (NBTTagCompound bpt : blueprints) {
            list.appendTag(bpt);
        }
        // Save it as an uncompressed BCv1
        // We do this so other people can use their gzip libraries to decompress the contents easily
        byte[] data = NbtSquisher.squishBuildCraftV1Uncompressed(main);

        try (GZIPOutputStream gzip = new GZIPOutputStream(new FileOutputStream(file))) {
            DataOutputStream dos = new DataOutputStream(gzip);
            dos.writeInt(data.length);
            gzip.write(data);
            blueprints.clear();
        } catch (IOException io) {
            // BIG problem, we might have just lost all of the large blueprints :/
            io.printStackTrace();
        }
    }

    private static File getSaveFile(MinecraftServer server) {
        File save = server.getActiveAnvilConverter().getFile(server.getFolderName(), "data");
        save.mkdir();
        return new File(save, "blueprints.bcc.gz");
    }
}
