package buildcraft.builders;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.builders.BlockFrame.EFrameConnection;
import buildcraft.core.Box;
import buildcraft.core.blueprints.Blueprint;

public class PatternQuarryFrame {
    public static final PatternQuarryFrame INSTANCE = new PatternQuarryFrame();

    public Blueprint getBlueprint(Box box, World world) {
        Blueprint result = new Blueprint(box.size());

        int x = box.size().getX() - 1;
        int y = box.size().getY() - 1;
        int z = box.size().getZ() - 1;
        for (int d = 1; d < x; d++) {
            result.set(new BlockPos(d, 0, 0), EFrameConnection.EAST_WEST.getSchematic());
            result.set(new BlockPos(d, 0, z), EFrameConnection.EAST_WEST.getSchematic());
            result.set(new BlockPos(d, y, 0), EFrameConnection.EAST_WEST.getSchematic());
            result.set(new BlockPos(d, y, z), EFrameConnection.EAST_WEST.getSchematic());
        }

        for (int d = 1; d < y; d++) {
            result.set(new BlockPos(0, d, 0), EFrameConnection.UP_DOWN.getSchematic());
            result.set(new BlockPos(0, d, z), EFrameConnection.UP_DOWN.getSchematic());
            result.set(new BlockPos(x, d, 0), EFrameConnection.UP_DOWN.getSchematic());
            result.set(new BlockPos(x, d, z), EFrameConnection.UP_DOWN.getSchematic());
        }

        for (int d = 1; d < z; d++) {
            result.set(new BlockPos(0, 0, d), EFrameConnection.NORTH_SOUTH.getSchematic());
            result.set(new BlockPos(0, y, d), EFrameConnection.NORTH_SOUTH.getSchematic());
            result.set(new BlockPos(x, 0, d), EFrameConnection.NORTH_SOUTH.getSchematic());
            result.set(new BlockPos(x, y, d), EFrameConnection.NORTH_SOUTH.getSchematic());
        }

        result.set(new BlockPos(0, 0, 0), EFrameConnection.SOUTH_EAST_UP.getSchematic());
        result.set(new BlockPos(0, 0, z), EFrameConnection.NORTH_EAST_UP.getSchematic());
        result.set(new BlockPos(x, 0, 0), EFrameConnection.SOUTH_WEST_UP.getSchematic());
        result.set(new BlockPos(x, 0, z), EFrameConnection.NORTH_WEST_UP.getSchematic());

        result.set(new BlockPos(0, y, 0), EFrameConnection.SOUTH_EAST_DOWN.getSchematic());
        result.set(new BlockPos(0, y, z), EFrameConnection.NORTH_EAST_DOWN.getSchematic());
        result.set(new BlockPos(x, y, 0), EFrameConnection.SOUTH_WEST_DOWN.getSchematic());
        result.set(new BlockPos(x, y, z), EFrameConnection.NORTH_WEST_DOWN.getSchematic());

        return result;
    }
}
