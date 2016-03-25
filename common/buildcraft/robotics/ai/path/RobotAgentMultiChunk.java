package buildcraft.robotics.ai.path;

import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import buildcraft.robotics.path.IAgent;
import buildcraft.robotics.path.IVirtualSpaceAccessor;
import buildcraft.robotics.path.IVirtualSpaceAccessor.IVirtualDestination;
import buildcraft.robotics.path.IVirtualSpaceAccessor.IVirtualPoint;
import buildcraft.robotics.path.MiniChunkCache;
import buildcraft.robotics.path.MiniChunkGraph.MiniChunkNode;
import buildcraft.robotics.path.SinglePointDestination;

public class RobotAgentMultiChunk implements IAgent<MiniChunkNode> {
    private final MiniChunkNode current;
    private final IVirtualDestination<MiniChunkNode> destination;

    public static RobotAgentMultiChunk create(World world, IVirtualSpaceAccessor<MiniChunkNode> space, BlockPos current, BlockPos dest) {
        // Send the request for the second node (without waiting for it) so that when we have finished waiting for the
        // first it might have either finished or nearly finished.
        MiniChunkCache.requestGraph(world, dest);
        MiniChunkNode currentNode = MiniChunkCache.requestAndWait(world, current).getFor(current);
        MiniChunkNode destNode = MiniChunkCache.requestAndWait(world, dest).getFor(dest);

        IVirtualPoint<MiniChunkNode> point = space.getPoint(destNode);
        IVirtualDestination<MiniChunkNode> destinationNode = new SinglePointDestination<>(space, point);
        return new RobotAgentMultiChunk(currentNode, destinationNode);
    }

    public RobotAgentMultiChunk(MiniChunkNode current, IVirtualDestination<MiniChunkNode> destination) {
        this.current = current;
        this.destination = destination;
    }

    @Override
    public MiniChunkNode getCurrentPos() {
        return current;
    }

    @Override
    public IVirtualDestination<MiniChunkNode> getDestination() {
        return destination;
    }
}
