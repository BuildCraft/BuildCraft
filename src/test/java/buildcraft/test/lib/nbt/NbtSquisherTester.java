package buildcraft.test.lib.nbt;

import buildcraft.api.data.NbtSquishConstants;
import buildcraft.lib.misc.HashUtil;
import buildcraft.lib.misc.ProfilerUtil;
import buildcraft.lib.nbt.NbtSquisher;
import com.google.common.base.Stopwatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.profiling.ActiveProfiler;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class NbtSquisherTester {
    private static final String[] IDS = { //
            "minecraft:dirt", "minecraft:cooked_steak", "minecraft:cooked_beef", "minecraft:stick", //
            "minecraft:diamond", "buildcraftcore:gear_wood", "buildcraftcore:gear_stone"//
    };

    public static final CompoundTag nbt = genNbt(64 * 64 * 64);
    public static final CompoundTag nbtSmall = genNbt(10);

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

    private static CompoundTag genNbt(int bptSize) {
        Random rand = new Random(0x517123);

        CompoundTag nbt = new CompoundTag();
        nbt.putByte("primitive|byte", (byte) 1);
        nbt.putShort("primitive|short", (short) 2);
        nbt.putInt("primitive|int", 4);
        nbt.putLong("primitive|long", 6);
        nbt.putFloat("primitive|float", 10.01f);
        nbt.putDouble("primitive|double", 11.11010101010101001010);

        nbt.putByteArray("array|byte", new byte[] { 12, 13, 14 });
        nbt.putIntArray("array|int", new int[] { 15000, 160000, 17000, 180000 });

        nbt.putString("string", "OMG A VALUE");

        ListTag list = new ListTag();
        list.add(FloatTag.valueOf(19.91f));
        list.add(FloatTag.valueOf(19.45f));
        list.add(FloatTag.valueOf(19.41f));
        list.add(FloatTag.valueOf(19.32f));
        list.add(FloatTag.valueOf(19.76f));

        nbt.put("complex|list", list);

        nbt.put("complex|tag", new CompoundTag());

        CompoundTag compound = new CompoundTag();
        compound.putBoolean("a", false);
        compound.putDouble("b", 20.02);

        nbt.put("complex|compound", compound);

        String[] names = { "minecraft:air", "minecraft:log", "minecraft:torch", "minecraft:stone", "minecraft:fence" };
        int[] metas = { 1, 16, 5, 7, 4 };

        CompoundTag[] blocks = new CompoundTag[sum(metas)];

        int block = 0;
        for (int b = 0; b < names.length; b++) {
            CompoundTag blockNbt = new CompoundTag();
            blockNbt.putString("id", names[b]);
            blocks[block++] = blockNbt.copy();
            for (int m = 1; m < metas[b]; m++) {
                blockNbt.putByte("meta", (byte) m);
                blocks[block++] = blockNbt.copy();
            }
        }

        CompoundTag air = blocks[0];

        ListTag bpt = new ListTag();

        int chests = 0;
        for (int i = 0; i < bptSize; i++) {
            double r = rand.nextDouble();
            final CompoundTag toUse;
            if (r < 0.4) {
                toUse = air;
            } else if (r < 0.9999) {
                toUse = blocks[rand.nextInt(blocks.length)];
            } else {
                toUse = genRandomChest(rand);
                chests++;
            }
            bpt.add(toUse);
        }
        System.out.println(chests + " random chests in a " + Math.cbrt(bptSize) + " bpt");

        nbt.put("bpt", bpt);
        return nbt;
    }

    private static int sum(int[] values) {
        int total = 0;
        for (int i : values) {
            total += i;
        }
        return total;
    }

    public static long[] test(boolean print, CompoundTag nbt) throws IOException {
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

        CompoundTag to = NbtSquisher.expand(bytes.clone());
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

    public static void checkEquality(CompoundTag from, CompoundTag to) {
        String error = compoundEqual(from, to);
        if (!error.isEmpty()) {
            System.out.println(error);
            Assert.fail("Tags were not equal! (" + error + ")");
        }
    }

    private static String compoundEqual(CompoundTag from, CompoundTag to) {
        Set<String> keysFrom = from.getAllKeys();
        Set<String> keysTo = to.getAllKeys();
        if (!keysFrom.equals(keysTo)) {
            return "keys " + keysFrom + " -> " + keysTo;
        } else {
            for (String key : keysFrom) {
                Tag valFrom = from.get(key);
                Tag valTo = to.get(key);
                String err = nbtEquals(valFrom, valTo);
                if (!err.isEmpty()) {
                    return key + " = " + err;
                }
            }
            return "";
        }
    }

    private static String listEquals(ListTag from, ListTag to) {
        int l1 = from.size();
        int l2 = to.size();
        if (l1 != l2) {
            System.out.println("Differing lengths!");
            System.out.println("  from = " + l1);
            System.out.println("    to = " + l2);
            return "";
        } else {
            for (int i = 0; i < l1; i++) {
                Tag valFrom = from.get(i);
                Tag valTo = to.get(i);
                String err = nbtEquals(valFrom, valTo);
                if (!err.isEmpty()) {
                    return "[" + i + "] = " + err;
                }
            }
            return "";
        }
    }

    private static String nbtEquals(Tag valFrom, Tag valTo) {
        if (valFrom instanceof CompoundTag && valTo instanceof CompoundTag) {
            return compoundEqual((CompoundTag) valFrom, (CompoundTag) valTo);
        }
        if (valFrom instanceof ListTag && valTo instanceof ListTag) {
            return listEquals((ListTag) valFrom, (ListTag) valTo);
        }
        if (!valFrom.equals(valTo)) {
            return valFrom + " -> " + valTo;
        }
        return "";
    }

    private static CompoundTag genRandomChest(Random rand) {
        CompoundTag chest = new CompoundTag();
        chest.putString("block", "minecraft:chest");
        chest.putByte("meta", (byte) rand.nextInt(4));
        ListTag chestItems = new ListTag();

        CompoundTag itemB = genRandomItem(rand);
        int num = rand.nextInt(3) + 2;
        for (int i = 0; i < num; i++) {
            if (rand.nextInt(6) > 0) {
                chestItems.add(itemB);
            } else {
                chestItems.add(genRandomItem(rand));
            }
        }

        chest.put("items", chestItems);
        return chest;
    }

    private static CompoundTag genRandomItem(Random rand) {
        CompoundTag item = new CompoundTag();
        item.putString("id", IDS[rand.nextInt(IDS.length)]);
        item.putByte("Count", (byte) (16 + rand.nextInt(3) * 2));
        item.putShort("Damage", (short) 0);
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
        NbtSquisher.profiler.push("root");
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

        NbtSquisher.profiler.pop();
        if (NbtSquisher.profiler instanceof ActiveProfiler activeProfiler) {
//            ProfilerUtil.printProfilerResults(NbtSquisher.profiler, "root.write");
            ProfilerUtil.printProfilerResults(activeProfiler, "root.write");
        }
        watchWhole.stop();
        System.out.println("Whole test took " + watchWhole.elapsed(TimeUnit.MINUTES) + "m, "
                + watchWhole.elapsed(TimeUnit.SECONDS) % 60 + "s");
    }
}
