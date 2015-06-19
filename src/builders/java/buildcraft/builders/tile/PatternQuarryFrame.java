package buildcraft.builders.tile;

import net.minecraft.world.World;

import buildcraft.builders.block.BlockFrame.EFrameConnection;
import buildcraft.core.Box;
import buildcraft.core.blueprints.Blueprint;

public class PatternQuarryFrame {
    public static final PatternQuarryFrame INSTANCE = new PatternQuarryFrame();

    public Blueprint getBlueprint(Box box, World world) {
        Blueprint result = new Blueprint(box.sizeX(), box.sizeY(), box.sizeZ());

        for (int x = 1; x < box.sizeX() - 1; x++) {
            result.contents[x][0][0] = EFrameConnection.EAST_WEST.getSchematic();
            result.contents[x][0][box.sizeZ() - 1] = EFrameConnection.EAST_WEST.getSchematic();
            result.contents[x][box.sizeY() - 1][0] = EFrameConnection.EAST_WEST.getSchematic();
            result.contents[x][box.sizeY() - 1][box.sizeZ() - 1] = EFrameConnection.EAST_WEST.getSchematic();
        }

        for (int y = 1; y < box.sizeY() - 1; y++) {
            result.contents[0][y][0] = EFrameConnection.UP_DOWN.getSchematic();
            result.contents[0][y][box.sizeZ() - 1] = EFrameConnection.UP_DOWN.getSchematic();
            result.contents[box.sizeX() - 1][y][0] = EFrameConnection.UP_DOWN.getSchematic();
            result.contents[box.sizeX() - 1][y][box.sizeZ() - 1] = EFrameConnection.UP_DOWN.getSchematic();
        }

        return result;
    }
}
