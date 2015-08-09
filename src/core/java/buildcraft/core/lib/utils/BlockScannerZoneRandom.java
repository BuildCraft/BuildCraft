package buildcraft.core.lib.utils;

import java.util.Iterator;
import java.util.Random;

import net.minecraft.util.BlockPos;

import buildcraft.api.core.IZone;

public class BlockScannerZoneRandom implements Iterable<BlockPos> {

    private Random rand;
    private IZone zone;
    private BlockPos pos;

    class BlockIt implements Iterator<BlockPos> {

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public BlockPos next() {
            BlockPos pos = zone.getRandomBlockPos(rand);
            return pos.subtract(BlockScannerZoneRandom.this.pos);
        }

        @Override
        public void remove() {}
    }

    public BlockScannerZoneRandom(BlockPos pos, Random iRand, IZone iZone) {
        this.pos = pos;
        rand = iRand;
        zone = iZone;
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return new BlockIt();
    }

}
