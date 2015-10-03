package buildcraft.builders.tile;

import net.minecraft.world.World;

import buildcraft.builders.block.BlockFrame.EFrameConnection;
import buildcraft.core.Box;
import buildcraft.core.blueprints.Blueprint;

public class PatternQuarryFrame {
    public static final PatternQuarryFrame INSTANCE = new PatternQuarryFrame();

    public Blueprint getBlueprint(Box box, World world) {
        Blueprint result = new Blueprint(box.sizeX(), box.sizeY(), box.sizeZ());

        int x = box.sizeX() - 1;
        int y = box.sizeY() - 1;
        int z = box.sizeZ() - 1;
        for (int d = 1; d < x; d++) {
            result.contents[d][0][0] = EFrameConnection.EAST_WEST.getSchematic();
            result.contents[d][0][z] = EFrameConnection.EAST_WEST.getSchematic();
            result.contents[d][y][0] = EFrameConnection.EAST_WEST.getSchematic();
            result.contents[d][y][z] = EFrameConnection.EAST_WEST.getSchematic();
        }

        for (int d = 1; d < y; d++) {
            result.contents[0][d][0] = EFrameConnection.UP_DOWN.getSchematic();
            result.contents[0][d][z] = EFrameConnection.UP_DOWN.getSchematic();
            result.contents[x][d][0] = EFrameConnection.UP_DOWN.getSchematic();
            result.contents[x][d][z] = EFrameConnection.UP_DOWN.getSchematic();
        }

        for (int d = 1; d < z; d++) {
            result.contents[0][0][d] = EFrameConnection.NORTH_SOUTH.getSchematic();
            result.contents[0][y][d] = EFrameConnection.NORTH_SOUTH.getSchematic();
            result.contents[x][0][d] = EFrameConnection.NORTH_SOUTH.getSchematic();
            result.contents[x][y][d] = EFrameConnection.NORTH_SOUTH.getSchematic();
        }

        result.contents[0][0][0] = EFrameConnection.SOUTH_EAST_UP.getSchematic();
        result.contents[0][0][z] = EFrameConnection.NORTH_EAST_UP.getSchematic();
        result.contents[x][0][0] = EFrameConnection.SOUTH_WEST_UP.getSchematic();
        result.contents[x][0][z] = EFrameConnection.NORTH_WEST_UP.getSchematic();

        result.contents[0][y][0] = EFrameConnection.SOUTH_EAST_DOWN.getSchematic();
        result.contents[0][y][z] = EFrameConnection.NORTH_EAST_DOWN.getSchematic();
        result.contents[x][y][0] = EFrameConnection.SOUTH_WEST_DOWN.getSchematic();
        result.contents[x][y][z] = EFrameConnection.NORTH_WEST_DOWN.getSchematic();

        return result;
    }
}
