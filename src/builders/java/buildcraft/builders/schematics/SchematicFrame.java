package buildcraft.builders.schematics;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.builders.BuildCraftBuilders;
import buildcraft.builders.block.BlockFrame;
import buildcraft.builders.block.BlockFrame.EFrameConnection;

public class SchematicFrame extends SchematicBlock {
    public SchematicFrame(EFrameConnection connection) {
        state = BuildCraftBuilders.frameBlock.getDefaultState().withProperty(BlockFrame.CONNECTIONS, connection);
    }

    @Override
    public void rotateLeft(IBuilderContext context) {
        state = state.withProperty(BlockFrame.CONNECTIONS, BlockFrame.CONNECTIONS.getValue(state).rotateLeft());
    }
}
