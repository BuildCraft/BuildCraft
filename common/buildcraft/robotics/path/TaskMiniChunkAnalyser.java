package buildcraft.robotics.path;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.BCLog;
import buildcraft.core.lib.BCWorkerThreads;
import buildcraft.robotics.path.MiniChunkCalculationData.CalculationStep;

public class TaskMiniChunkAnalyser implements Runnable {
    private final MiniChunkCalculationData data;
    short id = 0;

    public TaskMiniChunkAnalyser(MiniChunkCalculationData data) {
        this.data = data;
    }

    @Override
    public void run() {
        synchronized (data) {
            data.step(CalculationStep.FILLED, CalculationStep.ANALYSING);
            analyse();
            data.step(CalculationStep.ANALYSING, CalculationStep.ANALYSED);
        }
        BCWorkerThreads.executeWorkTask(new TaskMiniChunkNodeCreation(data));
    }

    private void analyse() {
        for (BlockPos p : BlockPos.getAllInBox(BlockPos.ORIGIN, new BlockPos(15, 15, 15))) {
            if (data.graphArray[p.getX()][p.getY()][p.getZ()] != -1) continue;
            if (data.expenseArray[p.getX()][p.getY()][p.getZ()] < 0) continue;
            analyseImpl(p);
        }
        data.numNodes = id;
        BCLog.logger.info(data.min + " [analyser] Nodes: " + data.numNodes);
    }

    private void analyseImpl(BlockPos from) {
        Set<BlockPos> openSet = new HashSet<>();
        Set<BlockPos> closedSet = new HashSet<>();
        openSet.add(from);
        while (!openSet.isEmpty()) {
            BlockPos toTest = openSet.iterator().next();
            int expense = data.expenseArray[toTest.getX()][toTest.getY()][toTest.getZ()];
            if (expense > 0) {
                data.graphArray[toTest.getX()][toTest.getY()][toTest.getZ()] = id;
                for (EnumFacing face : EnumFacing.values()) {
                    BlockPos offset = toTest.offset(face);
                    if (!isValid(offset)) continue;
                    if (closedSet.contains(offset)) continue;
                    openSet.add(offset);
                }
            }
            openSet.remove(toTest);
            closedSet.add(toTest);
        }
        id++;
    }

    public static boolean isValid(BlockPos offset) {
        if (offset.getX() < 0 || offset.getX() >= 16) return false;
        if (offset.getY() < 0 || offset.getY() >= 16) return false;
        if (offset.getZ() < 0 || offset.getZ() >= 16) return false;
        return true;
    }
}
