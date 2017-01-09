package buildcraft.test.lib.nbt;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import org.junit.Test;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;

import buildcraft.lib.nbt.NBTSquishDebugging;
import buildcraft.lib.nbt.NbtSquisher;

public class NbtSquisherTester {
    private final NBTTagCompound nbt = genNbt();

    @Test
    public void testSimpleNBT() throws IOException {
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

        nbt.setTag("complex|tag", new NBTTagCompound());

        NBTTagCompound compound = new NBTTagCompound();
        compound.setBoolean("a", false);
        compound.setDouble("b", 20.02);

        nbt.setTag("complex|compound", compound);

        NBTTagCompound air = new NBTTagCompound();
        air.setString("block", "minecraft:air");

        NBTTagCompound diorite = new NBTTagCompound();
        diorite.setString("block", "minecraft:stone");
        diorite.setByte("meta", (byte) 4);

        NBTTagCompound andersite = new NBTTagCompound();
        andersite.setString("block", "minecraft:stone");
        andersite.setByte("meta", (byte) 3);

        NBTTagCompound cobblestone = new NBTTagCompound();
        cobblestone.setString("block", "minecraft:cobblestone");

        NBTTagCompound torch = new NBTTagCompound();
        torch.setString("block", "minecraft:torch");
        torch.setByte("meta", (byte) 0);

        NBTTagCompound itemApple = new NBTTagCompound();
        itemApple.setString("id", "minecraft:apple");
        itemApple.setByte("Count", (byte) 12);
        itemApple.setShort("Damage", (short) 0);

        NBTTagCompound chest = new NBTTagCompound();
        chest.setString("block", "minecraft:chest");
        chest.setByte("meta", (byte) 2);
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

        return bytes;
    }

    private static void testBcOnly(NBTTagCompound nbt) {
        Stopwatch watch = Stopwatch.createStarted();
        byte[] bytes = NbtSquisher.squishBuildCraftV1Uncompressed(nbt);
        watch.stop();
        printBytesData("buildcraft[un] took " + padMilliseconds(watch.elapsed(TimeUnit.MILLISECONDS), 8), bytes);
        watch.reset();

        watch.start();
        bytes = NbtSquisher.squishBuildCraftV1(nbt);
        watch.stop();
        printBytesData("buildcraft[cp] took " + padMilliseconds(watch.elapsed(TimeUnit.MILLISECONDS), 8), bytes);
    }

    public static void checkEquality(NBTTagCompound from, NBTTagCompound to) {
        if (!checkEquality("", from, to)) {
            // Assert.fail("Tags were not equal!");
        }
    }

    private static boolean checkEquality(String start, NBTTagCompound from, NBTTagCompound to) {
        Set<String> keysFrom = from.getKeySet();
        Set<String> keysTo = to.getKeySet();
        if (!keysFrom.equals(keysTo)) {
            System.out.println(start + "Differing keys!");
            System.out.println(start + "  from = " + keysFrom);
            System.out.println(start + "    to = " + keysTo);
            return false;
        } else {
            boolean wasEqual = false;
            start = "  " + start;
            for (String key : keysFrom) {
                String start2 = start + key + ":";
                NBTBase valFrom = from.getTag(key);
                NBTBase valTo = to.getTag(key);
                wasEqual &= checkEquality(start2, valFrom, valTo);
            }
            return wasEqual;
        }
    }

    private static boolean checkEquality(String start, NBTTagList from, NBTTagList to) {
        int l1 = from.tagCount();
        int l2 = to.tagCount();
        if (l1 != l2) {
            System.out.println(start + "Differing lengths!");
            System.out.println(start + "  from = " + l1);
            System.out.println(start + "    to = " + l2);
            return false;
        } else {
            boolean wasEqual = true;
            start = "  " + start;
            for (int i = 0; i < l1; i++) {
                String start2 = start + i + ":";
                NBTBase valFrom = from.get(i);
                NBTBase valTo = to.get(i);
                wasEqual &= checkEquality(start2, valFrom, valTo);
            }
            return wasEqual;
        }
    }

    private static boolean checkEquality(String start, NBTBase valFrom, NBTBase valTo) {
        if (valFrom instanceof NBTTagCompound && valTo instanceof NBTTagCompound) {
            return checkEquality(start, (NBTTagCompound) valFrom, (NBTTagCompound) valTo);
        }
        if (valFrom instanceof NBTTagList && valTo instanceof NBTTagList) {
            return checkEquality(start, (NBTTagList) valFrom, (NBTTagList) valTo);
        }
        if (!valFrom.equals(valTo)) {
            System.out.println(start + " were not equal!");
            return false;
        }
        return true;
    }

    private static NBTTagCompound genRandomChest(Random rand) {
        NBTTagCompound chest = new NBTTagCompound();
        chest.setString("block", "minecraft:chest");
        chest.setByte("meta", (byte) rand.nextInt(4));
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
        NbtSquisherTester tester = new NbtSquisherTester();
        for (int i = 0; i < 100; i++) {
            System.in.read();
            System.out.println("Starting test " + (i + 1));
            testBcOnly(tester.nbt);
            System.out.println("Finished test " + (i + 1));
        }
    }
}
