package buildcraft.robotics.ai;

import buildcraft.api.core.IBlockFilter;
import buildcraft.api.core.IZone;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.ResourceIdBlock;
import buildcraft.lib.blockscan.BlockScannerExpanding;
import buildcraft.lib.blockscan.BlockScannerRandom;
import buildcraft.lib.blockscan.BlockScannerZoneRandom;
import buildcraft.lib.misc.IterableAlgorithmRunner;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.pathfinding.PathFindingSearch;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;

import java.util.Iterator;
import java.util.LinkedList;

public class AIRobotSearchBlock extends AIRobot {

    public BlockPos blockFound;
    public LinkedList<BlockPos> path;
    private PathFindingSearch blockScanner = null;
    private IterableAlgorithmRunner blockScannerJob;
    private IBlockFilter pathFound;
    private Iterator<BlockPos> blockIter;
    private double maxDistanceToEnd;
    private IZone zone;

    public AIRobotSearchBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotSearchBlock(EntityRobotBase iRobot, boolean random, IBlockFilter iPathFound, double iMaxDistanceToEnd) {
        super(iRobot);

        pathFound = iPathFound;
        zone = iRobot.getZoneToWork();
        if (!random) {
            blockIter = new BlockScannerExpanding().iterator();
        } else {
            if (zone != null) {
                // BlockPos pos = new BlockPos(iRobot);
                BlockPos pos = iRobot.blockPosition();
                blockIter = new BlockScannerZoneRandom(pos, iRobot.level.random, zone).iterator();
            } else {
                blockIter = new BlockScannerRandom(iRobot.level.random, 64).iterator();
            }
        }
        blockFound = null;
        path = null;
        maxDistanceToEnd = iMaxDistanceToEnd;
    }

    @Override
    public void start() {
        // blockScanner = new PathFindingSearch(robot.level, new BlockPos(robot), blockIter, pathFound, maxDistanceToEnd, 96, zone);
        blockScanner = new PathFindingSearch(robot.level, robot.blockPosition(), blockIter, pathFound, maxDistanceToEnd, 96, zone);
        blockScannerJob = new IterableAlgorithmRunner(blockScanner);
        blockScannerJob.start();
    }

    @Override
    public void update() {
        if (blockScannerJob == null) {
            // This is probably due to a load from NBT. Abort the ai in
            // that case, since there's no filter to analyze either.
            abort();
            return;
        }

        if (blockScannerJob.isDone()) {
            path = blockScanner.getResult();

            if (path != null && path.size() > 0) {
                path.removeLast();
                blockFound = blockScanner.getResultTarget();
            } else {
                path = null;
            }

            terminate();
        }
    }

    @Override
    public void end() {
        if (blockScannerJob != null) {
            blockScannerJob.terminate();
        }
    }

    @Override
    public boolean success() {
        return blockFound != null;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);

        if (blockFound != null) {
            nbt.put("blockFound", NBTUtilBC.writeBlockPos(blockFound));
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.contains("blockFound")) {
            blockFound = NBTUtilBC.readBlockPos(nbt.get("blockFound"));
        }
    }

    public boolean takeResource() {
        boolean taken = false;
        if (robot.getRegistry().take(new ResourceIdBlock(blockFound), robot)) {
            taken = true;
        }
        unreserve();
        return taken;
    }

    public void unreserve() {
        blockScanner.unreserve(blockFound);
    }

    @Override
    // public int getEnergyCost()
    public long getPowerCost() {
        return 2 * MjAPI.MJ / 10;
    }

}
