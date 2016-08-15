package buildcraft.test.lib.nbt;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import org.junit.Test;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;

import buildcraft.lib.nbt.NBTSquishDebugging;
import buildcraft.lib.nbt.NbtSquisher;

public class NbtSquisherTester {
    @Test
    public void testSimpleNBT() throws IOException {
        NBTTagCompound nbt = genNbt();

        spool(200, nbt);

        NBTSquishDebugging.debug = true;
        test(nbt);
        NBTSquishDebugging.debug = false;
        test(nbt);
    }

    private static NBTTagCompound genNbt() {
        Random rand = new Random(0x517123);

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("primitive|byte", (byte) 1);
        nbt.setShort("primitive|short", (short) 2);
        nbt.setInteger("primitive|int", 4);
        nbt.setLong("primitive|long", 6);
        nbt.setFloat("primitive|float", 10.01f);
        nbt.setDouble("primitive|double", 11.11010101010101001010);

        nbt.setByteArray("array|byte", new byte[] { 12, 13, 14 });
        nbt.setIntArray("array|int", new int[] { 15000, 160000, 17000, 180000 });

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

        NBTTagCompound air = new NBTTagCompound();
        air.setString("block", "minecraft:air");

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
        chestItems.appendTag(itemApple);

        chest.setTag("items", chestItems);

        NBTTagList bpt = new NBTTagList();
        for (int i = 0; i < 64 * 64 * 64; i++) {
            double r = rand.nextDouble();
            final NBTTagCompound toUse;
            if (r < 0.8) toUse = air;
            else if (r < 0.875) toUse = andersite;
            else if (r < 0.93) toUse = cobblestone;
            else if (r < 0.9995) toUse = torch;
            else if (r < 0.9999) toUse = chest;
            else toUse = genRandomChest(rand);
            bpt.appendTag(toUse);
        }

        nbt.setTag("bpt", bpt);
        return nbt;
    }

    public static void spool(int times, NBTTagCompound nbt) {
        for (int i = 0; i < times; i++) {
            if (i % 10 == 0) {
                System.out.println("Spooling [ " + i + "/" + times + " ]");
            }
            try {
                warm(nbt);
            } catch (IOException io) {
                throw new Error(io);
            }
        }
    }

    public static void warm(NBTTagCompound nbt) throws IOException {
        NbtSquisher.expand(NbtSquisher.squishVanillaUncompressed(nbt));
        NbtSquisher.expand(NbtSquisher.squishVanilla(nbt));
        NbtSquisher.expand(NbtSquisher.squishBuildCraftV1Uncompressed(nbt));
        NbtSquisher.expand(NbtSquisher.squishBuildCraftV1(nbt));
    }

    public static byte[] test(NBTTagCompound nbt) throws IOException {
        Stopwatch watch = Stopwatch.createStarted();
        byte[] bytes = NbtSquisher.squishVanillaUncompressed(nbt);
        watch.stop();
        printBytesData("vanilla   [un] took " + padMilliseconds(watch.elapsed(TimeUnit.MILLISECONDS), 8), bytes);
        watch.reset();

        NBTTagCompound to = NbtSquisher.expand(bytes.clone());
        checkEquality(nbt, to);

        watch.start();
        bytes = NbtSquisher.squishVanilla(nbt);
        watch.stop();
        printBytesData("vanilla   [cp] took " + padMilliseconds(watch.elapsed(TimeUnit.MILLISECONDS), 8), bytes);
        watch.reset();

        to = NbtSquisher.expand(bytes.clone());
        checkEquality(nbt, to);

        watch.start();
        bytes = NbtSquisher.squishBuildCraftV1Uncompressed(nbt);
        watch.stop();
        printBytesData("buildcraft[un] took " + padMilliseconds(watch.elapsed(TimeUnit.MILLISECONDS), 8), bytes);
        watch.reset();

        to = NbtSquisher.expand(bytes.clone());
        checkEquality(nbt, to);

        watch.start();
        bytes = NbtSquisher.squishBuildCraftV1(nbt);
        watch.stop();
        printBytesData("buildcraft[cp] took " + padMilliseconds(watch.elapsed(TimeUnit.MILLISECONDS), 8), bytes);

        to = NbtSquisher.expand(bytes.clone());
        checkEquality(nbt, to);

        return bytes;
    }

    public static void checkEquality(NBTTagCompound from, NBTTagCompound to) {
        checkEquality("", from, to);
    }

    private static void checkEquality(String start, NBTTagCompound from, NBTTagCompound to) {
        Set<String> keysFrom = from.getKeySet();
        Set<String> keysTo = to.getKeySet();
        if (!keysFrom.equals(keysTo)) {
            System.out.println(start + "Differing keys!");
            System.out.println(start + "  from = " + keysFrom);
            System.out.println(start + "    to = " + keysTo);
        } else {
            start = "  " + start;
            for (String key : keysFrom) {
                String start2 = start + key + ":";
                NBTBase valFrom = from.getTag(key);
                NBTBase valTo = to.getTag(key);
                checkEquality(start2, valFrom, valTo);
            }
        }
    }

    private static void checkEquality(String start, NBTBase valFrom, NBTBase valTo) {
        if (!valFrom.equals(valTo)) {
            System.out.println(start + " were not equal!");
        }
    }

    private static NBTTagCompound genRandomChest(Random rand) {
        NBTTagCompound chest = new NBTTagCompound();
        chest.setString("block", "minecraft:chest");
        NBTTagCompound stateCmp = new NBTTagCompound();
        EnumFacing facing = EnumFacing.getHorizontal(rand.nextInt(4));
        stateCmp.setString("facing", facing.getName().toLowerCase(Locale.ROOT));
        chest.setTag("state", stateCmp);
        NBTTagList chestItems = new NBTTagList();

        NBTTagCompound itemB = genRandomItem(rand);
        int num = rand.nextInt(3) + rand.nextInt(3) - 3;
        num += 5;
        num *= 6;
        for (int i = 0; i < num; i++) {
            if (rand.nextInt(6) == 0) {
                chestItems.appendTag(itemB);
            } else {
                chestItems.appendTag(genRandomItem(rand));
            }
        }

        chest.setTag("items", chestItems);
        return chest;
    }

    private static final String[] IDS = { //
        "minecraft:dirt", "minecraft:cooked_steak", "minecraft:cooked_beef", "minecraft:stick",//
        "minecraft:diamond", "buildcraftcore:gear_wood", "buildcraftcore:gear_stone"//
    };

    private static NBTTagCompound genRandomItem(Random rand) {
        NBTTagCompound item = new NBTTagCompound();
        item.setString("id", IDS[rand.nextInt(IDS.length)]);
        item.setByte("Count", (byte) (16 + rand.nextInt(3) * 2));
        item.setShort("Damage", (short) 0);
        return item;
    }

    public static void printBytesData(String name, byte[] bytes) {
        String formatted = DecimalFormat.getInstance().format(bytes.length);
        System.out.print(name + "(" + pad(formatted, 13) + ") |");
        int max = Math.min(bytes.length, 200);
        for (int i = 0; i < max; i++) {
            printByte(bytes, i);
        }
        System.out.println();
    }

    private static String padMilliseconds(long name, int l) {
        return pad(NumberFormat.getInstance().format(name), l) + "ms ";
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
