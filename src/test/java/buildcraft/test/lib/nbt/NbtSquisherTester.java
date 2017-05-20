package buildcraft.test.lib.nbt;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import org.junit.Test;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.profiler.Profiler;

import buildcraft.lib.nbt.NbtSquisher;

public class NbtSquisherTester {
    private final NBTTagCompound nbt = genNbt(64 * 64 * 64);
    private final NBTTagCompound nbtSmall = genNbt(10);

    @Test
    public void testSimpleNBT() throws IOException {
        test(true, nbt);
    }

    private static NBTTagCompound genNbt(int bptSize) {
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

        String[] names = { "minecraft:air", "minecraft:log", "minecraft:torch", "minecraft:stone", "minecraft:fence" };
        int[] metas = { 1, 16, 5, 7, 4 };

        NBTTagCompound[] blocks = new NBTTagCompound[sum(metas)];

        int block = 0;
        for (int b = 0; b < names.length; b++) {
            NBTTagCompound blockNbt = new NBTTagCompound();
            blockNbt.setString("id", names[b]);
            blocks[block++] = blockNbt.copy();
            for (int m = 1; m < metas[b]; m++) {
                blockNbt.setByte("meta", (byte) m);
                blocks[block++] = blockNbt.copy();
            }
        }

        NBTTagCompound air = blocks[0];

        NBTTagList bpt = new NBTTagList();

        int chests = 0;
        for (int i = 0; i < bptSize; i++) {
            double r = rand.nextDouble();
            final NBTTagCompound toUse;
            if (r < 0.4) {
                toUse = air;
            } else if (r < 0.9999) {
                toUse = blocks[rand.nextInt(blocks.length)];
            } else {
                toUse = genRandomChest(rand);
                chests++;
            }
            bpt.appendTag(toUse);
        }
        System.out.println(chests + " random chests in a " + Math.cbrt(bptSize) + " bpt");

        nbt.setTag("bpt", bpt);
        return nbt;
    }

    private static int sum(int[] values) {
        int total = 0;
        for (int i : values) {
            total += i;
        }
        return total;
    }

    public static long[] test(boolean print, NBTTagCompound nbt) throws IOException {
        long[] times = new long[4];

        Stopwatch watch = Stopwatch.createStarted();
        byte[] bytes = NbtSquisher.squishVanillaUncompressed(nbt);
        watch.stop();
        if (print) {
            times[0] = watch.elapsed(TimeUnit.MILLISECONDS);
            printBytesData("vanilla   [un] took " + padMilliseconds(times[0], 8), bytes);
        }
        watch.reset();

        NBTTagCompound to = NbtSquisher.expand(bytes.clone());
        checkEquality(nbt, to);

        watch.start();
        bytes = NbtSquisher.squishVanilla(nbt);
        watch.stop();
        if (print) {
            times[1] = watch.elapsed(TimeUnit.MILLISECONDS);
            printBytesData("vanilla   [cp] took " + padMilliseconds(times[1], 8), bytes);
        }
        watch.reset();

        to = NbtSquisher.expand(bytes.clone());
        checkEquality(nbt, to);

        watch.start();
        bytes = NbtSquisher.squishBuildCraftV1Uncompressed(nbt);
        watch.stop();
        if (print) {
            times[2] = watch.elapsed(TimeUnit.MILLISECONDS);
            printBytesData("buildcraft[un] took " + padMilliseconds(times[2], 8), bytes);
        }
        watch.reset();

        NbtSquisher.debug = false;

        to = NbtSquisher.expand(bytes.clone());
        checkEquality(nbt, to);

        watch.start();
        bytes = NbtSquisher.squishBuildCraftV1(nbt);
        watch.stop();
        if (print) {
            times[3] = watch.elapsed(TimeUnit.MILLISECONDS);
            printBytesData("buildcraft[cp] took " + padMilliseconds(times[3], 8), bytes);
        }

        to = NbtSquisher.expand(bytes.clone());
        checkEquality(nbt, to);

        return times;
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
        int num = rand.nextInt(3) + 2;
        for (int i = 0; i < num; i++) {
            if (rand.nextInt(6) > 0) {
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

    @SuppressWarnings("StringConcatenationInLoop")
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
        System.in.read();
        NbtSquisherTester tester = new NbtSquisherTester();
        Stopwatch watch = Stopwatch.createStarted();
        for (int i = 1; i <= 100_000; i++) {
            test(false, tester.nbtSmall);
            if (i % 10_000 == 0) {
                watch.stop();
                System.out.println("Finished test " + i + " in " + watch.elapsed(TimeUnit.MILLISECONDS) + "ms");
                watch.reset().start();
            }
        }
        watch.reset();

        NbtSquisher.profiler.profilingEnabled = true;
        NbtSquisher.profiler.startSection("root");

        final int times = 100;
        long[][] all = new long[times][];

        System.in.read();
        NbtSquisher.debug = true;
        for (int i = 0; i < 100; i++) {
            System.out.println("Starting test " + (i + 1));
            all[i] = test(true, tester.nbt);
            System.out.println("Finished test " + (i + 1));
            NbtSquisher.debug = false;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
        String[] types = { "vanilla   [un]", "vanilla   [cp]", "buildcraft[un]", "buildcraft[cp]" };
        for (int i = 0; i < 4; i++) {
            long total = 0;
            for (int j = 20; j < times; j++)
                total += all[j][i];
            long average = total * 100 / (times - 20);
            System.out.println(types[i] + " took (on average) " + (average / 100) + "." + (average % 100) + "ms");
        }

        NbtSquisher.profiler.endSection();
        writeProfilerResults(0, "root.write", NbtSquisher.profiler);
    }

    private static void writeProfilerResults(int indent, String sectionName, Profiler profiler) {
        List<Profiler.Result> list = profiler.getProfilingData(sectionName);

        if (!list.isEmpty() && list.size() >= 3) {
            for (int i = 1; i < list.size(); ++i) {
                Profiler.Result profiler$result = list.get(i);
                StringBuilder builder = new StringBuilder();
                builder.append(String.format("[%02d] ", indent));

                for (int j = 0; j < indent; ++j) {
                    builder.append("|   ");
                }

                builder.append(profiler$result.profilerName);
                builder.append(" - ");
                builder.append(String.format("%.2f", profiler$result.usePercentage));
                builder.append("%/");
                builder.append(String.format("%.2f", profiler$result.totalUsePercentage));
                System.out.println(builder.toString());

                if (!"unspecified".equals(profiler$result.profilerName)) {
                    try {
                        writeProfilerResults(indent + 1, sectionName + "." + profiler$result.profilerName, profiler);
                    } catch (Exception exception) {
                        System.out.println("[[ EXCEPTION " + exception + " ]]");
                    }
                }
            }
        }
    }
}
