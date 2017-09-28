package buildcraft.test.lib.misc.collect;

import org.junit.Assert;
import org.junit.Test;

import buildcraft.lib.misc.collect.TypedMap;
import buildcraft.lib.misc.collect.TypedMapDirect;
import buildcraft.lib.misc.collect.TypedMapHierarchy;

public class TypedMapTester {

    interface IRandomInterface {
        int getRandom();
    }

    interface INamedInterface {
        String getName();
    }

    class Independant {}

    enum RandomE implements IRandomInterface {
        A,
        B;

        @Override
        public int getRandom() {
            return ordinal();
        }
    }

    enum Both implements IRandomInterface, INamedInterface {
        A,
        B;

        @Override
        public String getName() {
            return name();
        }

        @Override
        public int getRandom() {
            return 15 + ordinal();
        }
    }

    @Test
    public void testDirect() {
        TypedMap<Object> map = new TypedMapDirect<>();
        map.put(RandomE.A);
        map.put(Both.A);
        Independant i = new Independant();
        map.put(i);

        Assert.assertEquals(null, map.get(IRandomInterface.class));
        Assert.assertEquals(null, map.get(INamedInterface.class));
        Assert.assertEquals(i, map.get(Independant.class));
        Assert.assertEquals(RandomE.A, map.get(RandomE.class));
        Assert.assertEquals(Both.A, map.get(Both.class));

        map.put(RandomE.B);

        Assert.assertEquals(RandomE.B, map.get(RandomE.class));

        map.remove(RandomE.B);

        Assert.assertEquals(null, map.get(RandomE.class));
    }

    @Test
    public void testMult() {
        TypedMap<Object> map = new TypedMapHierarchy<>();
        map.put(RandomE.A);
        map.put(Both.A);
        Independant i = new Independant();
        map.put(i);

        Assert.assertNotEquals(null, map.get(IRandomInterface.class));
        Assert.assertEquals(Both.A, map.get(INamedInterface.class));
        Assert.assertEquals(i, map.get(Independant.class));
        Assert.assertEquals(RandomE.A, map.get(RandomE.class));
        Assert.assertEquals(Both.A, map.get(Both.class));
    }
}
