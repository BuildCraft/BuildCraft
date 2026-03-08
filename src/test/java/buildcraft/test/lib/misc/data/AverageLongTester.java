package buildcraft.test.lib.misc.data;

import buildcraft.lib.misc.data.AverageLong;
import net.minecraft.nbt.CompoundTag;
import org.junit.Assert;
import org.junit.Test;

public class AverageLongTester {
    @Test
    public void testNbt() {
        final long val = 0xDC_BA_98_76_54_32_10L;

        AverageLong avg = new AverageLong(5);
        AverageLong avg2 = new AverageLong(5);
        avg.tick(val);
        avg.tick(val);
        avg.tick(val);
        avg.tick(val);
        avg.tick(val);
        avg.tick(val);

        CompoundTag nbt = new CompoundTag();
        avg.writeToNbt(nbt, "test");
        avg2.readFromNbt(nbt, "test");
        Assert.assertEquals(avg.getAverageLong(), avg2.getAverageLong());
    }
}
