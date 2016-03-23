package buildcraft.robotics.path;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import buildcraft.robotics.path.MiniChunkCalculationData.CalculationStep;

public class MiniChunkAnalyser implements Runnable {
    private final MiniChunkCalculationData data;

    public MiniChunkAnalyser(MiniChunkCalculationData data) {
        this.data = data;
    }

    @Override
    public void run() {
        if (data.step != CalculationStep.FILLED) {
            throw new IllegalStateException("Wrong state! Expected FILLED but found " + data.step + " [no-sync]");
        }
        synchronized (data) {
            if (data.step != CalculationStep.FILLED) {
                throw new IllegalStateException("Wrong state! Expected FILLED but found " + data.step + " [synchronized]");
            }
            data.step = CalculationStep.ANALYSING;
            analyse();
            data.step = CalculationStep.ANALYSED;
        }
        MiniChunkCache.WORKER_THREAD_POOL.execute(new MiniChunkNodeCreation(data));
    }

    private void analyse() {
        Set<BlockPos> analysed = new HashSet<>();
        for (BlockPos p : BlockPos.getAllInBox(BlockPos.ORIGIN, new BlockPos(16, 16, 16))) {
            if (analysed.contains(p)) continue;
            analyseImpl(analysed, p);
            analysed.add(p);
        }
    }

    private void analyseImpl(Set<BlockPos> analysed, BlockPos from) {
        Set<BlockPos> partOfThis = new HashSet<>();
        Set<BlockPos> openSet = new HashSet<>();
        partOfThis.add(from);
        openSet.add(from);
        while (!openSet.isEmpty()) {
            BlockPos toTest = openSet.iterator().next();
            int expense = data.blockData[toTest.getX()][toTest.getY()][toTest.getZ()];
            if (expense < 100) {
                partOfThis.add(toTest);
                for (EnumFacing face : EnumFacing.values()) {
                    BlockPos offset = toTest.offset(face);
                    if (!isValid(offset)) continue;
                    if (partOfThis.contains(offset)) continue;
                    if (analysed.contains(toTest)) throw new IllegalStateException(
                            "Should not be able to re-analyse an already analysed block! (this = " + toTest + ", set = " + analysed + ")");
                    openSet.add(from);
                }
            }
            openSet.remove(toTest);
        }
        analysed.addAll(partOfThis);
        data.joinedGraphs.add(partOfThis);
    }

    public static boolean isValid(BlockPos offset) {
        if (offset.getX() < 0 || offset.getX() >= 16) return false;
        if (offset.getY() < 0 || offset.getY() >= 16) return false;
        if (offset.getZ() < 0 || offset.getZ() >= 16) return false;
        return true;
    }
}
