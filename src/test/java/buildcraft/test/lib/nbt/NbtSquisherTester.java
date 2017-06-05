package buildcraft.test.lib.nbt;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;

import org.junit.Assert;
import org.junit.Test;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.profiler.Profiler;

import buildcraft.api.data.NbtSquishConstants;

import buildcraft.lib.misc.HashUtil;
import buildcraft.lib.nbt.NbtSquisher;

public class NbtSquisherTester {
    private static final String[] IDS = { //
        "minecraft:dirt", "minecraft:cooked_steak", "minecraft:cooked_beef", "minecraft:stick",//
        "minecraft:diamond", "buildcraftcore:gear_wood", "buildcraftcore:gear_stone"//
    };

    public static final NBTTagCompound nbt = genNbt(64 * 64 * 64);
    public static final NBTTagCompound nbtSmall = genNbt(10);

    @Test
    public void printSimpleBytes() {
        byte[] bytes = NbtSquisher.squish(nbtSmall, NbtSquishConstants.BUILDCRAFT_V1);
        char[] chars = new char[32];
        int len = bytes.length / 32;
        if (len * 32 < bytes.length) {
            len++;
        }
        for (int y = 0; y < len; y++) {
            for (int x = 0; x < 32; x++) {
                int idx = y * 32 + x;
                if (idx >= bytes.length) {
                    Arrays.fill(chars, x, 32, ' ');
                    break;
                }
                byte val = bytes[idx];
                int ubyte = Byte.toUnsignedInt(val);
                char c = (char) ubyte;
                if (!Character.isDefined(c) || Character.isISOControl(c)) {
                    c = '.';
                }
                chars[x] = c;
                String hex = Integer.toHexString(ubyte);
                if (hex.length() < 2) {
                    hex = " " + hex;
                }
                System.out.print(hex + " ");
            }
            System.out.println("|" + new String(chars));
        }
    }

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
        int msPadLength = 10;
        long[] times = new long[8];

        Stopwatch watch = Stopwatch.createStarted();
        byte[] bytes = NbtSquisher.squish(nbt, NbtSquishConstants.VANILLA);
        watch.stop();
        TimeUnit timeUnit = TimeUnit.MICROSECONDS;
        if (print) {
            times[0] = watch.elapsed(timeUnit);
            printBytesData("vanilla   [un|wr] took " + padMicroseconds(times[0], msPadLength), bytes);
        }
        watch.reset();

        NBTTagCompound to = NbtSquisher.expand(bytes.clone());
        checkEquality(nbt, to);

        watch.start();
        byte[] hash = HashUtil.computeHash(bytes);
        watch.stop();
        if (print) {
            times[4] = watch.elapsed(timeUnit);
            printBytesData("vanilla   [un|hs] took " + padMicroseconds(times[4], msPadLength), hash);
        }
        watch.reset();

        watch.start();
        bytes = NbtSquisher.squish(nbt, NbtSquishConstants.VANILLA_COMPRESSED);
        watch.stop();
        if (print) {
            times[1] = watch.elapsed(timeUnit);
            printBytesData("vanilla   [cp|wr] took " + padMicroseconds(times[1], msPadLength), bytes);
        }
        watch.reset();

        to = NbtSquisher.expand(bytes.clone());
        checkEquality(nbt, to);

        watch.start();
        hash = HashUtil.computeHash(bytes);
        watch.stop();
        if (print) {
            times[5] = watch.elapsed(timeUnit);
            printBytesData("vanilla   [cp|hs] took " + padMicroseconds(times[5], msPadLength), hash);
        }
        watch.reset();

        watch.start();
        bytes = NbtSquisher.squish(nbt, NbtSquishConstants.BUILDCRAFT_V1);
        watch.stop();
        if (print) {
            times[2] = watch.elapsed(timeUnit);
            printBytesData("buildcraft[un|wr] took " + padMicroseconds(times[2], msPadLength), bytes);
        }
        watch.reset();

        to = NbtSquisher.expand(bytes.clone());
        checkEquality(nbt, to);

        watch.start();
        hash = HashUtil.computeHash(bytes);
        watch.stop();
        if (print) {
            times[6] = watch.elapsed(timeUnit);
            printBytesData("buildcraft[un|hs] took " + padMicroseconds(times[6], msPadLength), hash);
        }
        watch.reset();

        NbtSquisher.debugBuffer = null;

        watch.start();
        bytes = NbtSquisher.squish(nbt, NbtSquishConstants.BUILDCRAFT_V1_COMPRESSED);
        watch.stop();
        if (print) {
            times[3] = watch.elapsed(timeUnit);
            printBytesData("buildcraft[cp|wr] took " + padMicroseconds(times[3], msPadLength), bytes);
        }
        watch.reset();

        to = NbtSquisher.expand(bytes.clone());
        checkEquality(nbt, to);

        watch.start();
        hash = HashUtil.computeHash(bytes);
        watch.stop();
        if (print) {
            times[7] = watch.elapsed(timeUnit);
            printBytesData("buildcraft[cp|hs] took " + padMicroseconds(times[7], msPadLength), hash);
        }
        watch.reset();

        return times;
    }

    public static void checkEquality(NBTTagCompound from, NBTTagCompound to) {
        String error = compoundEqual(from, to);
        if (!error.isEmpty()) {
            System.out.println(error);
            Assert.fail("Tags were not equal! (" + error + ")");
        }
    }

    private static String compoundEqual(NBTTagCompound from, NBTTagCompound to) {
        Set<String> keysFrom = from.getKeySet();
        Set<String> keysTo = to.getKeySet();
        if (!keysFrom.equals(keysTo)) {
            return "keys " + keysFrom + " -> " + keysTo;
        } else {
            for (String key : keysFrom) {
                NBTBase valFrom = from.getTag(key);
                NBTBase valTo = to.getTag(key);
                String err = nbtEquals(valFrom, valTo);
                if (!err.isEmpty()) {
                    return key + " = " + err;
                }
            }
            return "";
        }
    }

    private static String listEquals(NBTTagList from, NBTTagList to) {
        int l1 = from.tagCount();
        int l2 = to.tagCount();
        if (l1 != l2) {
            System.out.println("Differing lengths!");
            System.out.println("  from = " + l1);
            System.out.println("    to = " + l2);
            return "";
        } else {
            for (int i = 0; i < l1; i++) {
                NBTBase valFrom = from.get(i);
                NBTBase valTo = to.get(i);
                String err = nbtEquals(valFrom, valTo);
                if (!err.isEmpty()) {
                    return "[" + i + "] = " + err;
                }
            }
            return "";
        }
    }

    private static String nbtEquals(NBTBase valFrom, NBTBase valTo) {
        if (valFrom instanceof NBTTagCompound && valTo instanceof NBTTagCompound) {
            return compoundEqual((NBTTagCompound) valFrom, (NBTTagCompound) valTo);
        }
        if (valFrom instanceof NBTTagList && valTo instanceof NBTTagList) {
            return listEquals((NBTTagList) valFrom, (NBTTagList) valTo);
        }
        if (!valFrom.equals(valTo)) {
            return valFrom + " -> " + valTo;
        }
        return "";
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

    private static String padMicroseconds(long name, int l) {
        return pad(NumberFormat.getInstance().format(name), l) + "ųs ";
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

        Stopwatch watchWhole = Stopwatch.createStarted();

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

        final int times = 100;
        long[][] all = new long[times][];

        watchWhole.stop();
        System.in.read();
        watchWhole.start();

        // NbtSquisher.profiler.profilingEnabled = true;
        NbtSquisher.profiler.startSection("root");
        // NbtSquisher.debugBuffer = PrintingByteBuf::new;
        for (int i = 0; i < 100; i++) {
            System.out.println("Starting test " + (i + 1));
            all[i] = test(true, tester.nbt);
            System.out.println("Finished test " + (i + 1));
            // NbtSquisher.debugBuffer = null;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
        String[] types = { "vanilla   [un|wr]", "vanilla   [cp|wr]", "buildcraft[un|wr]", "buildcraft[cp|wr]",
            "vanilla   [un|hs]", "vanilla   [cp|hs]", "buildcraft[un|hs]", "buildcraft[cp|hs]" };
        for (int i = 0; i < 8; i++) {
            long total = 0;
            for (int j = 20; j < times; j++)
                total += all[j][i];
            long average = total / (times - 20);
            System.out.println(types[i] + " took (on average) " + padMicroseconds(average, 10));
        }

        NbtSquisher.profiler.endSection();
        writeProfilerResults(0, "root.write", NbtSquisher.profiler);
        watchWhole.stop();
        System.out.println("Whole test took " + watchWhole.elapsed(TimeUnit.MINUTES) + "m, "
            + watchWhole.elapsed(TimeUnit.SECONDS) % 60 + "s");
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
