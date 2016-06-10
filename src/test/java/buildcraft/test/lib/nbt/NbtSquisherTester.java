package buildcraft.test.lib.nbt;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import com.google.common.base.Stopwatch;

import org.junit.Test;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;

import buildcraft.lib.nbt.NBTSquishDebugging;
import buildcraft.lib.nbt.NbtSquisher;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;

public class NbtSquisherTester {
    @Test
    public void testSimpleNBT() throws IOException {
        Random rand = new Random(0x517123);

        NBTSquishDebugging.debug = true;
        NBTSquishDebugging.usesystem = true;

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("primitive|byte", (byte) 6);
        nbt.setShort("primitive|short", (short) 7);
        nbt.setInteger("primitive|int", 8);
        nbt.setLong("primitive|long", 9);
        nbt.setFloat("primitive|float", 10.01f);
        nbt.setDouble("primitive|double", 11.11);

        nbt.setByteArray("array|byte", new byte[] { 12, 13, 14 });
        nbt.setIntArray("array|int", new int[] { 15, 16, 17, 18 });

        nbt.setString("string", "OMG A VALUE");

        NBTTagList list = new NBTTagList();
        list.appendTag(new NBTTagFloat(19.91f));
        list.appendTag(new NBTTagFloat(19.45f));
        list.appendTag(new NBTTagFloat(19.41f));
        list.appendTag(new NBTTagFloat(19.32f));
        list.appendTag(new NBTTagFloat(19.76f));

        nbt.setTag("complex|list", list);

        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("a", false);
        compound.setDouble("b", 20.02);

        nbt.setTag("complex|compound", compound);

        NBTTagCompound diorite = new NBTTagCompound();
        diorite.setString("block", "minecraft:stone");
        NBTTagCompound stateCmp = new NBTTagCompound();
        stateCmp.setString("variant", "diorite");
        diorite.setTag("state", stateCmp);

        NBTTagCompound andersite = new NBTTagCompound();
        andersite.setString("block", "minecraft:stone");
        stateCmp = new NBTTagCompound();
        stateCmp.setString("variant", "andersite");
        andersite.setTag("state", stateCmp);

        NBTTagCompound cobblestone = new NBTTagCompound();
        cobblestone.setString("block", "minecraft:cobblestone");

        NBTTagCompound torch = new NBTTagCompound();
        torch.setString("block", "minecraft:torch");
        stateCmp = new NBTTagCompound();
        stateCmp.setString("facing", "down");
        torch.setTag("state", stateCmp);

        NBTTagCompound itemApple = new NBTTagCompound();
        itemApple.setString("id", "minecraft:apple");
        itemApple.setByte("Count", (byte) 12);
        itemApple.setShort("Damage", (short) 0);

        NBTTagCompound chest = new NBTTagCompound();
        chest.setString("block", "minecraft:chest");
        stateCmp = new NBTTagCompound();
        stateCmp.setString("facing", "east");
        chest.setTag("state", stateCmp);
        NBTTagList chestItems = new NBTTagList();
        chestItems.appendTag(genRandomItem(rand));
        chestItems.appendTag(itemApple);
        chestItems.appendTag(genRandomItem(rand));
        chestItems.appendTag(itemApple);
        chestItems.appendTag(genRandomItem(rand));
        chestItems.appendTag(genRandomItem(rand));
        chestItems.appendTag(genRandomItem(rand));

        chest.setTag("items", chestItems);

        NBTTagList bpt = new NBTTagList();
        for (int i = 0; i < 64 * 64 * 64; i++) {
            double r = rand.nextDouble();
            NBTTagCompound toUse = diorite;
            if (r < 0.9) toUse = diorite;
            else if (r < 0.92) toUse = andersite;
            else if (r < 0.94) toUse = cobblestone;
            else if (r < 0.97) toUse = torch;
            else if (r < 0.99) toUse = chest;
            else toUse = genRandomChest(rand);
            bpt.appendTag(toUse);
        }

        nbt.setTag("bpt", bpt);

        test(nbt);
        NBTSquishDebugging.debug = false;
        NBTSquishDebugging.usesystem = false;

        for (int i = 0; i < 32; i++) {
            test(nbt);
        }
    }

    public static void test(NBTTagCompound nbt) throws IOException {
        Stopwatch watch = Stopwatch.createStarted();
        ByteBuf buf = Unpooled.buffer();
        DataOutput out = new ByteBufOutputStream(buf);
        CompressedStreamTools.write(nbt, out);
        watch.stop();
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        printBytesData("vanilla   [un] took " + pad(watch.elapsed(TimeUnit.MILLISECONDS), 8), bytes);

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        watch.reset();
        watch.start();
        CompressedStreamTools.writeCompressed(nbt, outStream);
        bytes = outStream.toByteArray();
        watch.stop();
        printBytesData("vanilla   [cp] took " + pad(watch.elapsed(TimeUnit.MILLISECONDS), 8), bytes);

        watch.reset();
        watch.start();
        bytes = NbtSquisher.squish(nbt);
        watch.stop();
        printBytesData("buildcraft[un] took " + pad(watch.elapsed(TimeUnit.MILLISECONDS), 8), bytes);

        watch.reset();
        watch.start();
        bytes = compress(bytes);
        watch.stop();
        printBytesData("buildcraft[cp] took " + pad(watch.elapsed(TimeUnit.MILLISECONDS), 8), bytes);
    }

    private static NBTTagCompound genRandomChest(Random rand) {
        NBTTagCompound chest = new NBTTagCompound();
        chest.setString("block", "minecraft:chest");
        NBTTagCompound stateCmp = new NBTTagCompound();
        EnumFacing facing = EnumFacing.getHorizontal(rand.nextInt(4));
        stateCmp.setString("facing", facing.getName().toLowerCase(Locale.ROOT));
        chest.setTag("state", stateCmp);
        NBTTagList chestItems = new NBTTagList();

        NBTTagCompound itemA = genRandomItem(rand);
        NBTTagCompound itemB = genRandomItem(rand);
        for (int i = 0; i < 32; i++) {
            chestItems.appendTag(itemA);
        }
        chestItems.appendTag(itemB);
        chestItems.appendTag(itemB);

        chest.setTag("items", chestItems);
        return chest;
    }

    private static NBTTagCompound genRandomItem(Random rand) {
        NBTTagCompound item = new NBTTagCompound();
        int id = rand.nextInt(2);
        final String itemid;
        if (id == 0) itemid = "minecraft:dirt";
        else /* if (id == 1) */ itemid = "minecraft:cooked_steak";
        item.setString("id", itemid);
        item.setByte("Count", (byte) (16 + rand.nextInt(16) * 2));
        item.setShort("Damage", (short) 0);
        return item;
    }

    private static byte[] compress(byte[] bytes) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream out = new GZIPOutputStream(baos)) {
            out.write(bytes);
        }
        return baos.toByteArray();
    }

    public static void printBytesData(String name, byte[] bytes) {
        String formatted = DecimalFormat.getInstance().format(bytes.length);
        System.out.print(name + "(" + pad(formatted, 13) + ") |");
        int max = Math.min(bytes.length, 1000);
        for (int i = 0; i < max; i++) {
            printByte(bytes, i);
        }
        System.out.println();
    }

    private static String pad(long name, int l) {
        return pad(NumberFormat.getInstance().format(name), l);
    }

    private static String pad(String name, int l) {
        while (name.length() < l) {
            name = " " + name;
        }
        return name;
    }

    private static void printByte(byte[] bytes, int i) {
        int us = Byte.toUnsignedInt(bytes[i]);
        String hex = Integer.toHexString(us);
        if (hex.length() == 1) {
            hex = "0" + hex;
        }
        System.out.print(" " + hex);
    }

    public static void main(String[] args) throws IOException {
        new NbtSquisherTester().testSimpleNBT();
    }
}
