package buildcraft.lib.blockscan;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;

import java.util.Iterator;
import java.util.Random;

public class BlockScannerRandom implements Iterable<BlockPos> {

    private Random rand;
    private int maxDistance;

    class BlockIt implements Iterator<BlockPos> {

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public BlockPos next() {
            double radius = rand.nextFloat() * maxDistance;
            float polarAngle = rand.nextFloat() * 2.0F * (float) Math.PI;
            float azimuthAngle = rand.nextFloat() * (float) Math.PI;

            int searchX = (int) (radius * Mth.cos(polarAngle) * Mth.sin(azimuthAngle));
            int searchY = (int) (radius * Mth.cos(azimuthAngle));
            int searchZ = (int) (radius * Mth.sin(polarAngle) * Mth.sin(azimuthAngle));

            return new BlockPos(searchX, searchY, searchZ);
        }

        @Override
        public void remove() {}
    }

    public BlockScannerRandom(Random iRand, int iMaxDistance) {
        rand = iRand;
        maxDistance = iMaxDistance;
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return new BlockIt();
    }

}
